package com.ruoyi.kms.service.impl;

import java.util.List;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.kms.mapper.AnalysisResultMapper;
import com.ruoyi.kms.domain.AnalysisResult;
import com.ruoyi.kms.domain.RealTimeData;
import com.ruoyi.kms.domain.SystemMetadata;
import com.ruoyi.kms.mapper.SystemMetadataMapper;
import com.ruoyi.kms.service.IAnalysisResultService;

/**
 * 预警结果Service业务层处理
 *
 * @author hby
 * @date 2025-09-27
 */
@Service
public class AnalysisResultServiceImpl implements IAnalysisResultService
{
    @Autowired
    private AnalysisResultMapper analysisResultMapper;

    // @Autowired
    // private SystemMetadataMapper systemMetadataMapper;

    // 定义四个区间的阈值
    private static final BigDecimal LEVEL1_THRESHOLD = new BigDecimal("60");   // 60%
    private static final BigDecimal LEVEL2_THRESHOLD = new BigDecimal("80");   // 80%
    private static final BigDecimal LEVEL3_THRESHOLD = new BigDecimal("90");   // 90%
    private static final BigDecimal MAX_THRESHOLD = new BigDecimal("100");     // 100%

    /**
     * 查询预警结果
     */
    @Override
    public AnalysisResult selectAnalysisResultById(Long id)
    {
        return analysisResultMapper.selectAnalysisResultById(id);
    }

    /**
     * 查询预警结果列表
     */
    @Override
    public List<AnalysisResult> selectAnalysisResultList(AnalysisResult analysisResult)
    {
        return analysisResultMapper.selectAnalysisResultList(analysisResult);
    }

    /**
     * 新增预警结果
     */
    @Override
    public int insertAnalysisResult(AnalysisResult analysisResult)
    {
        analysisResult.setAnalysisTime(DateUtils.getNowDate());
        return analysisResultMapper.insertAnalysisResult(analysisResult);
    }

    /**
     * 修改预警结果
     */
    @Override
    public int updateAnalysisResult(AnalysisResult analysisResult)
    {
        return analysisResultMapper.updateAnalysisResult(analysisResult);
    }

    /**
     * 批量删除预警结果
     */
    @Override
    public int deleteAnalysisResultByIds(Long[] ids)
    {
        return analysisResultMapper.deleteAnalysisResultByIds(ids);
    }

    /**
     * 删除预警结果信息
     */
    @Override
    public int deleteAnalysisResultById(Long id)
    {
        return analysisResultMapper.deleteAnalysisResultById(id);
    }

//    /**
//     * 核心方法：根据新采集的数据，判断是否生成预警
//     */
//    @Override
//    public void generateWarning(RealTimeData realTimeData) {
//        // 查询各类资源的多级预警阈值
//        // CPU阈值：基础、中级、严重
//        SystemMetadata cpuBasicMeta = systemMetadataMapper.selectByModuleAndKey("analyzer", "cpuThresholdBasic");
//        SystemMetadata cpuMediumMeta = systemMetadataMapper.selectByModuleAndKey("analyzer", "cpuThresholdMedium");
//        SystemMetadata cpuSevereMeta = systemMetadataMapper.selectByModuleAndKey("analyzer", "cpuThresholdSevere");
//
//        // 内存阈值：基础、中级、严重
//        SystemMetadata memBasicMeta = systemMetadataMapper.selectByModuleAndKey("analyzer", "memThresholdBasic");
//        SystemMetadata memMediumMeta = systemMetadataMapper.selectByModuleAndKey("analyzer", "memThresholdMedium");
//        SystemMetadata memSevereMeta = systemMetadataMapper.selectByModuleAndKey("analyzer", "memThresholdSevere");
//
//        // 磁盘阈值：基础、中级、严重
//        SystemMetadata diskBasicMeta = systemMetadataMapper.selectByModuleAndKey("analyzer", "diskThresholdBasic");
//        SystemMetadata diskMediumMeta = systemMetadataMapper.selectByModuleAndKey("analyzer", "diskThresholdMedium");
//        SystemMetadata diskSevereMeta = systemMetadataMapper.selectByModuleAndKey("analyzer", "diskThresholdSevere");
//
//        // 健壮性检查：如果任何一个配置不存在，则不进行预警
//        if (cpuBasicMeta == null || cpuMediumMeta == null || cpuSevereMeta == null ||
//                memBasicMeta == null || memMediumMeta == null || memSevereMeta == null ||
//                diskBasicMeta == null || diskMediumMeta == null || diskSevereMeta == null) {
//            return;
//        }
//
//        // 转换阈值为BigDecimal
//        BigDecimal cpuBasic = new BigDecimal(cpuBasicMeta.getConfigValue());
//        BigDecimal cpuMedium = new BigDecimal(cpuMediumMeta.getConfigValue());
//        BigDecimal cpuSevere = new BigDecimal(cpuSevereMeta.getConfigValue());
//
//        BigDecimal memBasic = new BigDecimal(memBasicMeta.getConfigValue());
//        BigDecimal memMedium = new BigDecimal(memMediumMeta.getConfigValue());
//        BigDecimal memSevere = new BigDecimal(memSevereMeta.getConfigValue());
//
//        BigDecimal diskBasic = new BigDecimal(diskBasicMeta.getConfigValue());
//        BigDecimal diskMedium = new BigDecimal(diskMediumMeta.getConfigValue());
//        BigDecimal diskSevere = new BigDecimal(diskSevereMeta.getConfigValue());
//
//        // 获取当前资源使用值
//        BigDecimal currentCpu = realTimeData.getCpuUsage();
//        BigDecimal currentMem = realTimeData.getMemUsage();
//        BigDecimal currentDisk = realTimeData.getDiskUsage();
//
//        // 判断CPU预警级别
//        if (currentCpu.compareTo(cpuSevere) > 0) {
//            createWarningRecord(realTimeData.getId(), "CPU过高", "严重预警");
//        } else if (currentCpu.compareTo(cpuMedium) > 0) {
//            createWarningRecord(realTimeData.getId(), "CPU过高", "中级预警");
//        } else if (currentCpu.compareTo(cpuBasic) > 0) {
//            createWarningRecord(realTimeData.getId(), "CPU过高", "基础预警");
//        }
//
//        // 判断内存预警级别
//        if (currentMem.compareTo(memSevere) > 0) {
//            createWarningRecord(realTimeData.getId(), "内存过高", "严重预警");
//        } else if (currentMem.compareTo(memMedium) > 0) {
//            createWarningRecord(realTimeData.getId(), "内存过高", "中级预警");
//        } else if (currentMem.compareTo(memBasic) > 0) {
//            createWarningRecord(realTimeData.getId(), "内存过高", "基础预警");
//        }
//
//        // 判断磁盘预警级别
//        if (currentDisk.compareTo(diskSevere) > 0) {
//            createWarningRecord(realTimeData.getId(), "磁盘过高", "严重预警");
//        } else if (currentDisk.compareTo(diskMedium) > 0) {
//            createWarningRecord(realTimeData.getId(), "磁盘过高", "中级预警");
//        } else if (currentDisk.compareTo(diskBasic) > 0) {
//            createWarningRecord(realTimeData.getId(), "磁盘过高", "基础预警");
//        }
//    }

    /**
     * 核心方法：根据新采集的数据，判断是否生成预警
     * 使用四个阈值区间：
     * - 60%以下：无预警
     * - 60%-80%：基础预警
     * - 80%-90%：中级预警
     * - 90%-100%：严重预警
     */
    @Override
    public void generateWarning(RealTimeData realTimeData) {
        // 获取当前资源使用值
        BigDecimal currentCpu = realTimeData.getCpuUsage();
        BigDecimal currentMem = realTimeData.getMemUsage();
        BigDecimal currentDisk = realTimeData.getDiskUsage();

        // 判断CPU预警级别
        determineWarningLevel(realTimeData.getId(), "CPU过高", currentCpu);

        // 判断内存预警级别
        determineWarningLevel(realTimeData.getId(), "内存过高", currentMem);

        // 判断磁盘预警级别
        determineWarningLevel(realTimeData.getId(), "磁盘过高", currentDisk);
    }

    /**
     * 辅助方法：根据资源使用率确定预警级别并创建记录
     * @param dataId 关联的实时数据ID
     * @param warningType 预警类型（如 "CPU过高"）
     * @param usage 资源使用率
     */
    private void determineWarningLevel(Long dataId, String warningType, BigDecimal usage) {
        // 确保使用率不为null且在合理范围内
        if (usage == null || usage.compareTo(BigDecimal.ZERO) < 0 || usage.compareTo(MAX_THRESHOLD) > 0) {
            return;
        }

        String warningLevel = null;

        // 90%-100%：严重预警
        if (usage.compareTo(LEVEL3_THRESHOLD) >= 0) {
            warningLevel = "严重预警";
        }
        // 80%-90%：中级预警
        else if (usage.compareTo(LEVEL2_THRESHOLD) >= 0) {
            warningLevel = "中级预警";
        }
        // 60%-80%：基础预警
        else if (usage.compareTo(LEVEL1_THRESHOLD) >= 0) {
            warningLevel = "基础预警";
        }

        // 如果确定了预警级别，则创建预警记录
        if (warningLevel != null) {
            createWarningRecord(dataId, warningType, warningLevel);
        }
        // 60%以下：无预警，不做处理
    }

    /**
     * 辅助方法：创建并插入一条预警记录到数据库
     * @param dataId 关联的实时数据ID
     * @param warningType 预警类型（如 "CPU过高"）
     * @param warningLevel 预警级别（基础预警、中级预警、严重预警）
     */
    private void createWarningRecord(Long dataId, String warningType, String warningLevel) {
        AnalysisResult warning = new AnalysisResult();
        warning.setDataId(dataId);
        warning.setWarningType(warningType);
        warning.setWarningLevel(warningLevel);  // 使用传入的预警级别
        warning.setIsHandled(0);

        this.insertAnalysisResult(warning);
    }

    /**
     * 查询预警数量
     */
    @Override
    public int selectWarningCount(Integer isHandled) {
        return analysisResultMapper.selectWarningCount(isHandled);
    }
}
