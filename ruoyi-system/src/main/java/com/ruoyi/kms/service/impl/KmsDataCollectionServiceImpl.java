package com.ruoyi.kms.service.impl;

import com.ruoyi.kms.domain.RealTimeData;
import com.ruoyi.kms.service.IAnalysisResultService;
import com.ruoyi.kms.service.IKmsDataCollectionService;
import com.ruoyi.kms.service.IRealTimeDataService;
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
import java.math.RoundingMode;
import java.util.Date;

@Service // 关键：将此类标记为Spring的服务Bean
public class KmsDataCollectionServiceImpl implements IKmsDataCollectionService {
    private static final Logger log = LoggerFactory.getLogger(KmsDataCollectionServiceImpl.class);
    @Autowired // 关键：注入数据存储Service
    private IRealTimeDataService realTimeDataService;

    @Autowired // 关键：注入预警分析Service
    private IAnalysisResultService analysisResultService;

    // Oshi系统信息工具，用于采集硬件指标
    private static final SystemInfo SYSTEM_INFO = new SystemInfo();
    private static final HardwareAbstractionLayer HARDWARE = SYSTEM_INFO.getHardware();
    private static final OperatingSystem OS = SYSTEM_INFO.getOperatingSystem();

    @Override
    public void executeCollection() {
        log.info("--- KMS数据采集服务开始执行 ---");
        try {
            // 1. 采集系统指标
            BigDecimal cpuUsage = getCpuUsage();
            BigDecimal memUsage = getMemUsage();
            BigDecimal diskUsage = getDiskUsage();

            log.info(String.format("指标采集完成 - CPU: %.2f%%, 内存: %.2f%%, 磁盘: %.2f%%", cpuUsage, memUsage, diskUsage));

            // 2. 封装数据并存储到数据库
            RealTimeData data = new RealTimeData();
            data.setCpuUsage(cpuUsage);
            data.setMemUsage(memUsage);
            data.setDiskUsage(diskUsage);
            data.setCollectTime(new Date());
            int insertRows = realTimeDataService.insertRealTimeData(data);
            if (insertRows > 0) {
                log.info("数据已成功写入 real_time_data 表，ID: {}", data.getId());
                // 3. 触发预警分析
                analysisResultService.generateWarning(data);
            } else {
                log.error("数据写入 real_time_data 表失败！");
            }
            log.info("--- KMS数据采集服务执行完毕 ---");

        } catch (Exception e) {
            log.error("KMS数据采集服务执行失败，发生严重异常！", e);
            // 抛出异常，让上层调度框架（Quartz）捕获并记录失败
            throw new RuntimeException("数据采集服务内部错误", e);
        }
    }

    /**
     * 获取CPU使用率（%）
     */
    private BigDecimal getCpuUsage() throws InterruptedException {
        CentralProcessor processor = HARDWARE.getProcessor();

        // 1. 获取CPU负载的“前一次”快照
        long[] prevTicks = processor.getSystemCpuLoadTicks();

        // 2. 必须休眠一段时间（例如1秒）才能计算出有意义的使用率
        Thread.sleep(1000);

        // 3. 再次获取CPU负载快照（Oshi 4.x 的API会自动计算与上一次的差值）
        // 注意：这里不再需要手动获取 currTicks
        double load = processor.getSystemCpuLoadBetweenTicks(prevTicks);

        // 4. 将结果转换为百分比并返回
        return BigDecimal.valueOf(load * 100).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取内存使用率（%）
     */
    private BigDecimal getMemUsage() {
        GlobalMemory memory = HARDWARE.getMemory();
        long totalMem = memory.getTotal();
        long availableMem = memory.getAvailable();
        double usage = (double) (totalMem - availableMem) / totalMem;
        return BigDecimal.valueOf(usage * 100).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取磁盘使用率（%）
     */
    private BigDecimal getDiskUsage() {
        FileSystem fileSystem = OS.getFileSystem();
        for (OSFileStore store : fileSystem.getFileStores()) {
            // 过滤本地磁盘分区 (NTFS for Windows, ext4/xfs for Linux)
            if (store.getType().equalsIgnoreCase("NTFS") || store.getType().equalsIgnoreCase("ext4") || store.getType().equalsIgnoreCase("xfs")) {
                long totalSpace = store.getTotalSpace();
                long freeSpace = store.getUsableSpace();
                if (totalSpace > 0) {
                    double usage = (double) (totalSpace - freeSpace) / totalSpace;
                    return BigDecimal.valueOf(usage * 100).setScale(2, RoundingMode.HALF_UP);
                }
            }
        }
        return BigDecimal.valueOf(0.00).setScale(2, RoundingMode.HALF_UP);
    }
}