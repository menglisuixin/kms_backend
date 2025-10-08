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
            // 1. 采集CPU（1次/采集周期，所有磁盘共用）
            KmsCpu cpu = collectCpu();
            // 2. 采集内存（1次/采集周期，所有磁盘共用）
            KmsMem mem = collectMem();
            // 3. 采集所有有效磁盘（过滤虚拟分区，保留实际磁盘）
            List<KmsSysFile> diskList = collectAllDisks();

            // 4. 打印采集日志（便于调试）
            printCollectionLog(cpu, mem, diskList);

            // 5. 循环保存每个磁盘的完整记录（1个磁盘1条记录）
            Date collectTime = new Date(); // 所有记录统一采集时间
            int successCount = 0;
            for (KmsSysFile disk : diskList) {
                RealTimeData data = buildRealTimeData(cpu, mem, disk, collectTime);
                // 保存到数据库
                int rows = realTimeDataService.insertRealTimeData(data);
                if (rows > 0) {
                    successCount++;
                    log.info("磁盘[{}]数据保存成功，记录ID：{}", disk.getDirName(), data.getId());
                    // 触发预警判断（每个磁盘单独判断）
                    analysisResultService.generateWarning(data);
                } else {
                    log.error("磁盘[{}]数据保存失败", disk.getDirName());
                }
            }

            log.info("--- KMS数据采集服务执行完毕：共采集{}个磁盘，成功保存{}条记录 ---", diskList.size(), successCount);

        } catch (Exception e) {
            log.error("KMS数据采集服务执行异常", e);
            throw new RuntimeException("数据采集失败", e);
        }
    }

    /**
     * 构建RealTimeData对象（封装CPU/内存/磁盘数据）
     */
    private RealTimeData buildRealTimeData(KmsCpu cpu, KmsMem mem, KmsSysFile disk, Date collectTime) throws JsonProcessingException {
        RealTimeData data = new RealTimeData();

        // 1. 封装CPU数据
        data.setCpuUsage(BigDecimal.valueOf(Arith.round(cpu.getTotal() * 100, 2))); // 转为百分比（原0-1→0-100）
        data.setCpuUserUsage(BigDecimal.valueOf(Arith.round(cpu.getUsed() * 100, 2)));
        data.setCpuSysUsage(BigDecimal.valueOf(Arith.round(cpu.getSys() * 100, 2)));
        data.setCpuIdleUsage(BigDecimal.valueOf(Arith.round(cpu.getFree() * 100, 2)));
        data.setCpuCoreNum(cpu.getCpuNum());

        // 2. 封装内存数据（转GB，保留2位小数）
        double memTotalGb = Arith.div(mem.getTotal(), 1024 * 1024 * 1024, 2);
        double memUsedGb = Arith.div(mem.getUsed(), 1024 * 1024 * 1024, 2);
        double memFreeGb = Arith.div(mem.getFree(), 1024 * 1024 * 1024, 2);
        double memUsage = Arith.mul(Arith.div(mem.getUsed(), mem.getTotal(), 4), 100); // 使用率百分比

        data.setMemTotal(BigDecimal.valueOf(memTotalGb));
        data.setMemUsed(BigDecimal.valueOf(memUsedGb));
        data.setMemFree(BigDecimal.valueOf(memFreeGb));
        data.setMemUsage(BigDecimal.valueOf(Arith.round(memUsage, 2)));

        // 3. 封装磁盘数据（转为JSON字符串）
        Map<String, Object> diskMap = new HashMap<>();
        diskMap.put("path", disk.getDirName());       // 磁盘路径（如C:/）
        diskMap.put("type", disk.getTypeName());      // 磁盘类型（如NTFS）
        diskMap.put("total", Arith.round(Double.parseDouble(disk.getTotal().replace("GB", "")), 2)); // 总大小（GB）
        diskMap.put("used", Arith.round(Double.parseDouble(disk.getUsed().replace("GB", "")), 2));   // 已用（GB）
        diskMap.put("free", Arith.round(Double.parseDouble(disk.getFree().replace("GB", "")), 2));   // 剩余（GB）
        diskMap.put("usage", Arith.round(disk.getUsage(), 2)); // 使用率（百分比）

        data.setDiskData(objectMapper.writeValueAsString(diskMap));

        // 4. 基础字段
        data.setCollectTime(collectTime);
        data.setIsValid(1); // 1=有效数据

        return data;
    }

    /**
     * 采集CPU信息（Oshi采集，返回使用率百分比）
     */
    private KmsCpu collectCpu() throws InterruptedException {
        CentralProcessor processor = HARDWARE.getProcessor();
        KmsCpu cpu = new KmsCpu();
        cpu.setCpuNum(processor.getLogicalProcessorCount());

        // 采集CPU Tick快照（间隔1秒，确保使用率准确）
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        TimeUnit.SECONDS.sleep(1);
        long[] currTicks = processor.getSystemCpuLoadTicks();

        // 计算各状态Tick差值
        long userTick = currTicks[CentralProcessor.TickType.USER.ordinal()] - prevTicks[CentralProcessor.TickType.USER.ordinal()];
        long sysTick = currTicks[CentralProcessor.TickType.SYSTEM.ordinal()] - prevTicks[CentralProcessor.TickType.SYSTEM.ordinal()];
        long waitTick = currTicks[CentralProcessor.TickType.IOWAIT.ordinal()] - prevTicks[CentralProcessor.TickType.IOWAIT.ordinal()];
        long idleTick = currTicks[CentralProcessor.TickType.IDLE.ordinal()] - prevTicks[CentralProcessor.TickType.IDLE.ordinal()];

        // 总使用Tick（非空闲）
        long totalUsedTick = userTick + sysTick + waitTick;
        totalUsedTick = totalUsedTick == 0 ? 1 : totalUsedTick; // 避免分母为0
        // 总Tick（所有状态）
        long totalTick = totalUsedTick + idleTick;
        totalTick = totalTick == 0 ? 1 : totalTick;

        // 计算使用率（0-1，后续转百分比）
        cpu.setTotal((double) totalUsedTick / totalTick);       // 总使用率
        cpu.setUsed((double) userTick / totalUsedTick);         // 用户使用率（占总使用的比例）
        cpu.setSys((double) sysTick / totalUsedTick);           // 系统使用率（占总使用的比例）
        cpu.setWait((double) waitTick / totalUsedTick);         // 等待使用率（占总使用的比例）
        cpu.setFree((double) idleTick / totalTick);             // 空闲率

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
                Arith.round(cpu.getTotal() * 100, 2),
                Arith.round(cpu.getUsed() * 100, 2),
                Arith.round(cpu.getSys() * 100, 2),
                Arith.round(cpu.getFree() * 100, 2));

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