package com.ruoyi.kms.service.impl;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class KmsDataCollectionServiceImpl implements IKmsDataCollectionService {
    private static final Logger log = LoggerFactory.getLogger(KmsDataCollectionServiceImpl.class);
    @Autowired
    private IRealTimeDataService realTimeDataService;
    @Autowired
    private IAnalysisResultService analysisResultService;

    // Oshi核心工具
    private static final SystemInfo SYSTEM_INFO = new SystemInfo();
    private static final HardwareAbstractionLayer HARDWARE = SYSTEM_INFO.getHardware();
    private static final OperatingSystem OS = SYSTEM_INFO.getOperatingSystem();

    @Override
    public void executeCollection() {
        log.info("--- KMS数据采集服务开始执行（复用若依实体，多磁盘支持）---");
        try {
            // 1. 采集CPU、内存（只采集1次，所有磁盘记录共用）
            KmsCpu cpu = collectCpu();
            KmsMem mem = collectMem();
            // 2. 采集所有有效磁盘（返回C/D/E等磁盘列表）
            List<KmsSysFile> diskList = collectAllDisks();

            // 3. 打印采集日志（先打印CPU和内存，再打印每个磁盘）
            log.info("【CPU指标】核心数: {}个, 总使用率: {}%, 用户使用率: {}%, 系统使用率: {}%, 空闲率: {}%",
                    cpu.getCpuNum(),
                    Arith.round(cpu.getTotal(), 2),
                    Arith.round(cpu.getUsed(), 2),
                    Arith.round(cpu.getSys(), 2),
                    Arith.round(cpu.getFree(), 2));
            log.info("【内存指标】总量: {}GB, 已用: {}GB, 剩余: {}GB, 使用率: {}%",
                    Arith.round(mem.getTotal(), 2),
                    Arith.round(mem.getUsed(), 2),
                    Arith.round(mem.getFree(), 2),
                    Arith.round(mem.getUsage(), 2));
            // 打印每个磁盘的日志
            for (KmsSysFile disk : diskList) {
                log.info("【磁盘指标】路径: {}, 类型: {}, 总量: {}, 已用: {}, 剩余: {}, 使用率: {}%",
                        disk.getDirName(),
                        disk.getTypeName(),
                        disk.getTotal(),
                        disk.getUsed(),
                        disk.getFree(),
                        Arith.round(disk.getUsage(), 2));
            }

            // 4. 循环保存每个磁盘的记录（核心修改：每个磁盘1条记录）
            Date currentTime = new Date(); // 所有记录用同一个采集时间，便于后续关联查询
            int successCount = 0;
            for (KmsSysFile disk : diskList) {
                RealTimeData data = new RealTimeData();

                // 4.1 填充CPU指标（所有磁盘记录共用同一套CPU数据）
                data.setCpuUsage(BigDecimal.valueOf(Arith.round(cpu.getTotal(), 2)));
                data.setCpuUserUsage(BigDecimal.valueOf(Arith.round(cpu.getUsed(), 2)));
                data.setCpuSysUsage(BigDecimal.valueOf(Arith.round(cpu.getSys(), 2)));
                data.setCpuIdleUsage(BigDecimal.valueOf(Arith.round(cpu.getFree(), 2)));
                data.setCpuCoreNum(cpu.getCpuNum());

                // 4.2 填充内存指标（所有磁盘记录共用同一套内存数据）
                data.setMemTotal(BigDecimal.valueOf(Arith.round(mem.getTotal(), 2)));
                data.setMemUsed(BigDecimal.valueOf(Arith.round(mem.getUsed(), 2)));
                data.setMemFree(BigDecimal.valueOf(Arith.round(mem.getFree(), 2)));
                data.setMemUsage(BigDecimal.valueOf(Arith.round(mem.getUsage(), 2)));

                // 4.3 填充当前磁盘的指标（每个记录对应不同磁盘）
                data.setDiskPath(disk.getDirName());
                data.setDiskType(disk.getTypeName());
                // 提取磁盘容量数字（去掉"GB"后缀）
                double diskTotalNum = Double.parseDouble(disk.getTotal().replace("GB", ""));
                double diskUsedNum = Double.parseDouble(disk.getUsed().replace("GB", ""));
                double diskFreeNum = Double.parseDouble(disk.getFree().replace("GB", ""));
                data.setDiskTotal(BigDecimal.valueOf(Arith.round(diskTotalNum, 2)));
                data.setDiskUsed(BigDecimal.valueOf(Arith.round(diskUsedNum, 2)));
                data.setDiskFree(BigDecimal.valueOf(Arith.round(diskFreeNum, 2)));
                data.setDiskUsage(BigDecimal.valueOf(Arith.round(disk.getUsage(), 2)));

                // 4.4 基础字段（所有记录用同一采集时间）
                data.setCollectTime(currentTime);
                data.setIsValid(1);

                // 4.5 保存当前磁盘的记录到数据库
                int insertRows = realTimeDataService.insertRealTimeData(data);
                if (insertRows > 0) {
                    successCount++;
                    log.info("磁盘[{}]数据已成功写入 real_time_data 表，ID: {}", disk.getDirName(), data.getId());
                    // （可选）每个磁盘都触发一次告警判断（如果需要按磁盘单独告警）
                    analysisResultService.generateWarning(data);
                } else {
                    log.error("磁盘[{}]数据写入 real_time_data 表失败！", disk.getDirName());
                }
            }

            log.info("--- KMS数据采集服务执行完毕，共采集{}个磁盘，成功保存{}条记录 ---", diskList.size(), successCount);

        } catch (Exception e) {
            log.error("KMS数据采集服务执行失败，发生严重异常！", e);
            throw new RuntimeException("数据采集服务内部错误", e);
        }
    }

    /**
     * 采集CPU信息（按若依逻辑传参，完美复用KmsCpu）
     */
    private KmsCpu collectCpu() throws InterruptedException {
        CentralProcessor processor = HARDWARE.getProcessor();
        KmsCpu cpu = new KmsCpu();
        cpu.setCpuNum(processor.getLogicalProcessorCount());

        // 1. 获取CPU tick快照
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        TimeUnit.SECONDS.sleep(1);
        long[] currTicks = processor.getSystemCpuLoadTicks();

        // 2. 计算各部分tick差值
        long userTick = currTicks[CentralProcessor.TickType.USER.ordinal()] - prevTicks[CentralProcessor.TickType.USER.ordinal()];
        long sysTick = currTicks[CentralProcessor.TickType.SYSTEM.ordinal()] - prevTicks[CentralProcessor.TickType.SYSTEM.ordinal()];
        long waitTick = currTicks[CentralProcessor.TickType.IOWAIT.ordinal()] - prevTicks[CentralProcessor.TickType.IOWAIT.ordinal()];
        long idleTick = currTicks[CentralProcessor.TickType.IDLE.ordinal()] - prevTicks[CentralProcessor.TickType.IDLE.ordinal()];

        // 总使用tick（非空闲的总和）
        long totalUsedTick = userTick + sysTick + waitTick;
        totalUsedTick = totalUsedTick == 0 ? 1 : totalUsedTick; // 防止分母为0

        // 总tick（所有类型的总和）
        long totalTick = totalUsedTick + idleTick;
        totalTick = totalTick == 0 ? 1 : totalTick;

        // 3. 按若依逻辑传参：传“占比”而非“具体值”
        cpu.setTotal((double) totalUsedTick / totalTick); // 总使用占比（0-1，比如10%传0.1）
        cpu.setUsed((double) userTick / totalUsedTick);   // 用户占总使用的比例（0-1，比如30%传0.3）
        cpu.setSys((double) sysTick / totalUsedTick);     // 系统占总使用的比例（0-1）
        cpu.setWait((double) waitTick / totalUsedTick);   // 等待占总使用的比例（0-1）
        cpu.setFree((double) idleTick / totalTick);       // 空闲占比（0-1，直接算，不依赖total）

        return cpu;
    }

    /**
     * 采集内存信息，封装到若依的Mem实体
     */
    private KmsMem collectMem() {
        GlobalMemory memory = HARDWARE.getMemory();
        KmsMem mem = new KmsMem();
        // 若依Mem的setXxx接收字节数，直接赋值（get时会自动转GB）
        mem.setTotal(memory.getTotal());
        mem.setUsed(memory.getTotal() - memory.getAvailable());
        mem.setFree(memory.getAvailable());
        return mem;
    }

    /**
     * 采集所有有效磁盘（过滤虚拟/临时分区，保留C/D/E等实际磁盘）
     */
    private List<KmsSysFile> collectAllDisks() {
        FileSystem fileSystem = OS.getFileSystem();
        List<KmsSysFile> diskList = new ArrayList<>();

        for (OSFileStore store : fileSystem.getFileStores()) {
            // 过滤规则：排除虚拟分区、临时分区，只保留实际磁盘（如C:/、D:/）
            String mountPath = store.getMount();
            // 1. 排除Linux系统的虚拟分区（如/dev/loop、/sys等）
            if (mountPath.startsWith("/dev/loop") || mountPath.startsWith("/sys") || mountPath.startsWith("/proc")) {
                continue;
            }
            // 2. 排除Windows的网络共享或临时分区（如\\\\server\\share）
            if (mountPath.startsWith("\\\\") || mountPath.contains("Temporary")) {
                continue;
            }
            // 3. 排除容量过小的分区（如小于1GB的分区，可根据需求调整）
            long totalSpace = store.getTotalSpace();
            if (totalSpace < 1024 * 1024 * 1024) { // 1GB = 1024*1024*1024字节
                continue;
            }

            // 封装磁盘信息（和原逻辑一致，只是每个磁盘都创建一个KmsSysFile）
            KmsSysFile sysFile = new KmsSysFile();
            sysFile.setDirName(mountPath); // 磁盘路径（如C:/、D:/）
            sysFile.setTypeName(store.getType()); // 磁盘类型（如NTFS）

            // 计算磁盘容量（转GB，保留2位小数）
            double totalGb = Arith.div(totalSpace, 1024 * 1024 * 1024, 4);
            double usedGb = Arith.div(totalSpace - store.getUsableSpace(), 1024 * 1024 * 1024, 4);
            double freeGb = Arith.div(store.getUsableSpace(), 1024 * 1024 * 1024, 4);
            double usage = Arith.mul(Arith.div(totalSpace - store.getUsableSpace(), totalSpace, 4), 100);

            sysFile.setTotal(Arith.round(totalGb, 2) + "GB");
            sysFile.setUsed(Arith.round(usedGb, 2) + "GB");
            sysFile.setFree(Arith.round(freeGb, 2) + "GB");
            sysFile.setUsage(Arith.round(usage, 2));

            diskList.add(sysFile);
        }
        return diskList;
    }
}