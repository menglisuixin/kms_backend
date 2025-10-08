package com.ruoyi.kms.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.kms.domain.server.KmsCpu;
import com.ruoyi.kms.domain.server.KmsMem;
import com.ruoyi.kms.domain.server.KmsSysFile;
import com.ruoyi.kms.domain.RealTimeData;
import com.ruoyi.kms.service.IAnalysisResultService;
import com.ruoyi.kms.service.IKmsDataCollectionService;
import com.ruoyi.kms.service.IRealTimeDataService;
import com.ruoyi.common.utils.Arith;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * KMS数据采集Service（仅负责CPU/内存/磁盘数据采集与存储，无预警逻辑）
 *
 * @author hby
 * @date 2025-09-28
 */
@Service
public class KmsDataCollectionServiceImpl implements IKmsDataCollectionService {
    private static final Logger log = LoggerFactory.getLogger(KmsDataCollectionServiceImpl.class);

    @Autowired
    private IRealTimeDataService realTimeDataService;

    @Autowired
    private IAnalysisResultService analysisResultService;

    // JSON工具：用于磁盘数据转JSON
    @Autowired
    private ObjectMapper objectMapper;

    // Oshi硬件信息采集工具（静态初始化，避免重复创建）
    private static final SystemInfo SYSTEM_INFO = new SystemInfo();
    private static final HardwareAbstractionLayer HARDWARE = SYSTEM_INFO.getHardware();
    private static final OperatingSystem OS = SYSTEM_INFO.getOperatingSystem();

    @Override
    public void executeCollection() {
        log.info("--- KMS数据采集服务开始执行 ---");
        try {
            // 1. 采集各类信息
            KmsCpu cpu = collectCpu();
            KmsMem mem = collectMem();
            List<KmsSysFile> diskList = collectAllDisks();

            // 2. 打印采集日志
            printCollectionLog(cpu, mem, diskList);

            // 3. 构建合并后的单条记录
            Date collectTime = new Date();
            RealTimeData data = buildMergedRealTimeData(cpu, mem, diskList, collectTime);

            // 4. 保存单条记录到数据库
            int rows = realTimeDataService.insertRealTimeData(data);
            if (rows > 0) {
                log.info("所有磁盘数据合并保存成功，记录ID：{}", data.getId());
                // 5. 循环触发每个磁盘的预警判断（传入单条记录+当前磁盘信息）
                for (KmsSysFile disk : diskList) {
                    // 调用重载方法，同时传入整条记录和当前磁盘信息
                    analysisResultService.generateWarning(data, disk);
                }
            } else {
                log.error("合并数据保存失败");
            }

            log.info("--- KMS数据采集服务执行完毕：共采集{}个磁盘，成功保存1条合并记录 ---", diskList.size());

        } catch (Exception e) {
            log.error("KMS数据采集服务执行异常", e);
            throw new RuntimeException("数据采集失败", e);
        }
    }

    /**
     * 构建合并后的RealTimeData对象（磁盘数据组装为JSON数组）// 改动点4：新增合并构建方法
     */
    private RealTimeData buildMergedRealTimeData(KmsCpu cpu, KmsMem mem, List<KmsSysFile> diskList, Date collectTime) throws JsonProcessingException {
        RealTimeData data = new RealTimeData();

        // 1. CPU数据（逻辑不变）
        data.setCpuUsage(BigDecimal.valueOf(Arith.round(cpu.getTotal(), 2)));
        data.setCpuUserUsage(BigDecimal.valueOf(Arith.round(cpu.getUsed(), 2)));
        data.setCpuSysUsage(BigDecimal.valueOf(Arith.round(cpu.getSys(), 2)));
        data.setCpuIdleUsage(BigDecimal.valueOf(Arith.round(cpu.getFree(), 2)));
        data.setCpuCoreNum(cpu.getCpuNum());

        // 2. 内存数据（逻辑不变）
        double memTotalGb = Arith.div(mem.getTotal(), 1024 * 1024 * 1024, 2);
        double memUsedGb = Arith.div(mem.getUsed(), 1024 * 1024 * 1024, 2);
        double memFreeGb = Arith.div(mem.getFree(), 1024 * 1024 * 1024, 2);
        double memUsage = Arith.mul(Arith.div(mem.getUsed(), mem.getTotal(), 4), 100);

        data.setMemTotal(BigDecimal.valueOf(memTotalGb));
        data.setMemUsed(BigDecimal.valueOf(memUsedGb));
        data.setMemFree(BigDecimal.valueOf(memFreeGb));
        data.setMemUsage(BigDecimal.valueOf(Arith.round(memUsage, 2)));

        // 3. 磁盘数据：多个磁盘组装为List<Map>，再序列化为JSON数组// 改动点5：磁盘数据改为数组格式
        List<Map<String, Object>> diskMapList = new ArrayList<>();
        for (KmsSysFile disk : diskList) {
            Map<String, Object> diskMap = new HashMap<>();
            diskMap.put("path", disk.getDirName());
            diskMap.put("type", disk.getTypeName());
            diskMap.put("total", Arith.round(Double.parseDouble(disk.getTotal().replace("GB", "")), 2));
            diskMap.put("used", Arith.round(Double.parseDouble(disk.getUsed().replace("GB", "")), 2));
            diskMap.put("free", Arith.round(Double.parseDouble(disk.getFree().replace("GB", "")), 2));
            diskMap.put("usage", Arith.round(disk.getUsage(), 2));
            diskMapList.add(diskMap);
        }
        // 序列化为JSON数组（如[{"path":"C:\\",...}, {...}]）
        data.setDiskData(objectMapper.writeValueAsString(diskMapList));

        // 4. 基础字段（逻辑不变）
        data.setCollectTime(collectTime);
        data.setIsValid(1);

        return data;
    }

    /**
     * 采集CPU信息（修复版：完整Tick状态+无多核错误+数值兜底）
     */
    private KmsCpu collectCpu() throws InterruptedException {
        CentralProcessor processor = HARDWARE.getProcessor();
        KmsCpu cpu = new KmsCpu();
        int cpuCoreNum = processor.getLogicalProcessorCount();
        cpu.setCpuNum(cpuCoreNum);

        // 1. 获取两次Tick快照（间隔1秒，确保计算1秒内的变化）
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        TimeUnit.SECONDS.sleep(1);
        long[] currTicks = processor.getSystemCpuLoadTicks();

        // 2. 计算所有CPU状态的Tick差值（补充NICE/IRQ/SOFTIRQ，确保总时间完整）
        long idleTick = currTicks[CentralProcessor.TickType.IDLE.ordinal()] - prevTicks[CentralProcessor.TickType.IDLE.ordinal()];
        long userTick = currTicks[CentralProcessor.TickType.USER.ordinal()] - prevTicks[CentralProcessor.TickType.USER.ordinal()];
        long sysTick = currTicks[CentralProcessor.TickType.SYSTEM.ordinal()] - prevTicks[CentralProcessor.TickType.SYSTEM.ordinal()];
        long ioWaitTick = currTicks[CentralProcessor.TickType.IOWAIT.ordinal()] - prevTicks[CentralProcessor.TickType.IOWAIT.ordinal()];
        // 补充遗漏的状态
        long niceTick = currTicks[CentralProcessor.TickType.NICE.ordinal()] - prevTicks[CentralProcessor.TickType.NICE.ordinal()]; // 低优先级用户态
        long irqTick = currTicks[CentralProcessor.TickType.IRQ.ordinal()] - prevTicks[CentralProcessor.TickType.IRQ.ordinal()]; // 硬中断
        long softIrqTick = currTicks[CentralProcessor.TickType.SOFTIRQ.ordinal()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.ordinal()]; // 软中断

        // 3. 计算总Tick差值（包含所有状态，确保总时间完整）
        long totalTick = idleTick + userTick + sysTick + ioWaitTick + niceTick + irqTick + softIrqTick;
        totalTick = totalTick == 0 ? 1 : totalTick; // 避免除零

        // 4. 计算核心指标（基于完整的总时间）
        double idleUsage = (double) idleTick / totalTick ; // 空闲率
        double totalUsage = (double) (totalTick - idleTick) / totalTick; // 总使用率（非空闲占比）
        // 用户使用率：包含普通用户态（USER）和低优先级用户态（NICE）
        double userUsage = (double) (userTick + niceTick) / totalTick;
        // 系统使用率：包含系统态（SYSTEM）、硬中断（IRQ）、软中断（SOFTIRQ）
        double sysUsage = (double) (sysTick + irqTick + softIrqTick) / totalTick;

        // 5. 数值兜底：强制限制在0-100%（避免极端计算误差）
        idleUsage = Math.max(0.0, Math.min(100.0, idleUsage));
        totalUsage = Math.max(0.0, Math.min(100.0, totalUsage));
        userUsage = Math.max(0.0, Math.min(100.0, userUsage));
        sysUsage = Math.max(0.0, Math.min(100.0, sysUsage));

        // 6. 赋值到KmsCpu
        cpu.setFree(idleUsage);
        cpu.setTotal(totalUsage);
        cpu.setUsed(userUsage);
        cpu.setSys(sysUsage);

        // 日志验证（可选，便于调试）
        log.debug("CPU采集结果：核心数={}，总使用率={}%，用户使用率={}%，系统使用率={}%，空闲率={}%",
                cpuCoreNum, Arith.round(totalUsage, 2), Arith.round(userUsage, 2),
                Arith.round(sysUsage, 2), Arith.round(idleUsage, 2));

        return cpu;
    }

    /**
     * 采集内存信息（Oshi采集，单位：字节）
     */
    private KmsMem collectMem() {
        GlobalMemory memory = HARDWARE.getMemory();
        KmsMem mem = new KmsMem();
        mem.setTotal(memory.getTotal());         // 总量（字节）
        mem.setUsed(memory.getTotal() - memory.getAvailable()); // 已用（字节）
        mem.setFree(memory.getAvailable());      // 可用（字节）
        return mem;
    }

    /**
     * 采集所有有效磁盘（过滤虚拟/临时分区）
     */
    private List<KmsSysFile> collectAllDisks() {
        FileSystem fileSystem = OS.getFileSystem();
        List<KmsSysFile> diskList = new ArrayList<>();

        for (OSFileStore store : fileSystem.getFileStores()) {
            String mountPath = store.getMount();
            long totalSpace = store.getTotalSpace();

            // 过滤规则：1. 排除虚拟分区 2. 排除临时分区 3. 排除小于1GB的分区
            if (mountPath.startsWith("/dev/loop") || mountPath.startsWith("/sys") || mountPath.startsWith("/proc") // Linux虚拟分区
                    || mountPath.startsWith("\\\\") || mountPath.contains("Temporary") // Windows网络/临时分区
                    || totalSpace < 1024 * 1024 * 1024) { // 小于1GB的分区
                continue;
            }

            // 封装磁盘信息
            KmsSysFile disk = new KmsSysFile();
            disk.setDirName(mountPath);
            disk.setTypeName(store.getType());

            // 计算磁盘容量（转GB，保留2位小数）
            double totalGb = Arith.div(totalSpace, 1024 * 1024 * 1024, 2);
            double usedGb = Arith.div(totalSpace - store.getUsableSpace(), 1024 * 1024 * 1024, 2);
            double freeGb = Arith.div(store.getUsableSpace(), 1024 * 1024 * 1024, 2);
            double usage = Arith.mul(Arith.div(totalSpace - store.getUsableSpace(), totalSpace, 4), 100);

            disk.setTotal(totalGb + "GB");
            disk.setUsed(usedGb + "GB");
            disk.setFree(freeGb + "GB");
            disk.setUsage(Arith.round(usage, 2));

            diskList.add(disk);
        }

        return diskList;
    }

    /**
     * 打印采集日志（便于调试）
     */
    private void printCollectionLog(KmsCpu cpu, KmsMem mem, List<KmsSysFile> diskList) {
        // CPU日志
        log.info("【CPU采集结果】核心数：{}，总使用率：{}%，用户使用率：{}%，系统使用率：{}%，空闲率：{}%",
                cpu.getCpuNum(),
                Arith.round(cpu.getTotal(), 2),
                Arith.round(cpu.getUsed(), 2),
                Arith.round(cpu.getSys(), 2),
                Arith.round(cpu.getFree(), 2));

        // 内存日志
        double memTotalGb = Arith.div(mem.getTotal(), 1024 * 1024 * 1024, 2);
        double memUsedGb = Arith.div(mem.getUsed(), 1024 * 1024 * 1024, 2);
        double memFreeGb = Arith.div(mem.getFree(), 1024 * 1024 * 1024, 2);
        double memUsage = Arith.mul(Arith.div(mem.getUsed(), mem.getTotal(), 4), 100);
        log.info("【内存采集结果】总量：{}GB，已用：{}GB，剩余：{}GB，使用率：{}%",
                memTotalGb, memUsedGb, memFreeGb, Arith.round(memUsage, 2));

        // 磁盘日志
        log.info("【磁盘采集结果】共采集{}个有效磁盘：", diskList.size());
        for (KmsSysFile disk : diskList) {
            log.info("  - 路径：{}，类型：{}，总量：{}，已用：{}，剩余：{}，使用率：{}%",
                    disk.getDirName(), disk.getTypeName(),
                    disk.getTotal(), disk.getUsed(), disk.getFree(),
                    disk.getUsage());
        }
    }
}