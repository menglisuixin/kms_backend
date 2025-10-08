package com.ruoyi.kms.service.impl;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.kms.domain.AnalysisResult;
import com.ruoyi.kms.domain.RealTimeData;
import com.ruoyi.kms.mapper.AnalysisResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.kms.mapper.SystemMetadataMapper;
import com.ruoyi.kms.domain.SystemMetadata;
import com.ruoyi.kms.service.ISystemMetadataService;

/**
 * 系统元数据Service业务层处理
 * 
 * @author hby
 * @date 2025-09-28
 */
@Service
public class SystemMetadataServiceImpl implements ISystemMetadataService 
{

    @Autowired
    private AnalysisResultMapper analysisResultMapper;

    @Autowired
    private SystemMetadataMapper systemMetadataMapper;

    /**
     * 核心方法：根据新采集的数据，判断是否生成预警
     */
    @Override
    public void generateWarning(RealTimeData realTimeData) {
        // 查询各类资源的三级预警阈值配置
        SystemMetadata cpuMetadata = systemMetadataMapper.selectByModuleAndKey("analyzer", "cpuUsage");
        SystemMetadata memMetadata = systemMetadataMapper.selectByModuleAndKey("analyzer", "memUsage");
        SystemMetadata diskMetadata = systemMetadataMapper.selectByModuleAndKey("analyzer", "diskUsage");

        // 健壮性检查
        if (cpuMetadata != null) {
            checkAndCreateWarning(realTimeData.getId(), "CPU过高",
                    realTimeData.getCpuUsage(), cpuMetadata);
        }

        if (memMetadata != null) {
            checkAndCreateWarning(realTimeData.getId(), "内存过高",
                    realTimeData.getMemUsage(), memMetadata);
        }

        if (diskMetadata != null) {
            checkAndCreateWarning(realTimeData.getId(), "磁盘过高",
                    realTimeData.getDiskUsage(), diskMetadata);
        }
    }

    /**
     * 检查资源使用率并创建预警记录
     */
    private void checkAndCreateWarning(Long dataId, String warningType,
                                       BigDecimal usage, SystemMetadata metadata) {
        if (usage == null) {
            return;
        }

        try {
            // 解析三级预警阈值
            BigDecimal level1 = new BigDecimal(metadata.getWarningLevel1Value());  // 60
            BigDecimal level2 = new BigDecimal(metadata.getWarningLevel2Value());  // 80
            BigDecimal level3 = new BigDecimal(metadata.getWarningLevel3Value());  // 90

            // 判断预警级别
            if (usage.compareTo(level3) >= 0) {
                createWarningRecord(dataId, warningType, "严重预警");
            } else if (usage.compareTo(level2) >= 0) {
                createWarningRecord(dataId, warningType, "中级预警");
            } else if (usage.compareTo(level1) >= 0) {
                createWarningRecord(dataId, warningType, "基础预警");
            }
        } catch (NumberFormatException e) {
            // 处理阈值格式错误的情况
            return;
        }
    }

    /**
     * 创建预警记录
     */
    private void createWarningRecord(Long dataId, String warningType, String warningLevel) {
        AnalysisResult warning = new AnalysisResult();
        warning.setDataId(dataId);
        warning.setWarningType(warningType);
        warning.setWarningLevel(warningLevel);
        warning.setIsHandled(0);
        this.insertAnalysisResult(warning);
    }

    @Override
    public int insertAnalysisResult(AnalysisResult analysisResult) {
        analysisResult.setAnalysisTime(DateUtils.getNowDate());
        return analysisResultMapper.insertAnalysisResult(analysisResult);
    }

    /**
     * 查询系统元数据
     *
     * @param id 系统元数据主键
     * @return 系统元数据
     */
    @Override
    public SystemMetadata selectSystemMetadataById(Long id)
    {
        return systemMetadataMapper.selectSystemMetadataById(id);
    }

    /**
     * 查询系统元数据列表
     *
     * @param systemMetadata 系统元数据
     * @return 系统元数据
     */
    @Override
    public List<SystemMetadata> selectSystemMetadataList(SystemMetadata systemMetadata)
    {
        return systemMetadataMapper.selectSystemMetadataList(systemMetadata);
    }

    /**
     * 新增系统元数据
     *
     * @param systemMetadata 系统元数据
     * @return 结果
     */
    @Override
    public int insertSystemMetadata(SystemMetadata systemMetadata)
    {
        return systemMetadataMapper.insertSystemMetadata(systemMetadata);
    }

    /**
     * 修改系统元数据
     *
     * @param systemMetadata 系统元数据
     * @return 结果
     */
    @Override
    public int updateSystemMetadata(SystemMetadata systemMetadata)
    {
        systemMetadata.setUpdateTime(DateUtils.getNowDate());
        return systemMetadataMapper.updateSystemMetadata(systemMetadata);
    }

    /**
     * 批量删除系统元数据
     *
     * @param ids 需要删除的系统元数据主键
     * @return 结果
     */
    @Override
    public int deleteSystemMetadataByIds(Long[] ids)
    {
        return systemMetadataMapper.deleteSystemMetadataByIds(ids);
    }

    /**
     * 删除系统元数据信息
     *
     * @param id 系统元数据主键
     * @return 结果
     */
    @Override
    public int deleteSystemMetadataById(Long id)
    {
        return systemMetadataMapper.deleteSystemMetadataById(id);
    }
}
