package com.ruoyi.kms.service.impl;

import java.util.List;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils; // 引入若依的日期工具类
import com.ruoyi.kms.mapper.AnalysisResultMapper;
import com.ruoyi.kms.domain.AnalysisResult;
import com.ruoyi.kms.domain.RealTimeData; // 引入实时数据实体
import com.ruoyi.kms.domain.SystemMetadata; // 引入系统元数据实体
import com.ruoyi.kms.mapper.SystemMetadataMapper; // 引入系统元数据Mapper
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

    // 新增：注入SystemMetadataMapper，用于查询预警阈值
    @Autowired
    private SystemMetadataMapper systemMetadataMapper;

    /**
     * 查询预警结果
     *
     * @param id 预警结果主键
     * @return 预警结果
     */
    @Override
    public AnalysisResult selectAnalysisResultById(Long id)
    {
        return analysisResultMapper.selectAnalysisResultById(id);
    }

    /**
     * 查询预警结果列表
     *
     * @param analysisResult 预警结果
     * @return 预警结果
     */
    @Override
    public List<AnalysisResult> selectAnalysisResultList(AnalysisResult analysisResult)
    {
        return analysisResultMapper.selectAnalysisResultList(analysisResult);
    }

    /**
     * 新增预警结果 (这个方法主要供系统内部调用，如generateWarning)
     *
     * @param analysisResult 预警结果
     * @return 结果
     */
    @Override
    public int insertAnalysisResult(AnalysisResult analysisResult)
    {
        // 新增：在插入前设置分析时间为当前时间
        analysisResult.setAnalysisTime(DateUtils.getNowDate());
        return analysisResultMapper.insertAnalysisResult(analysisResult);
    }

    /**
     * 修改预警结果 (例如：更新处理状态)
     *
     * @param analysisResult 预警结果
     * @return 结果
     */
    @Override
    public int updateAnalysisResult(AnalysisResult analysisResult)
    {
        return analysisResultMapper.updateAnalysisResult(analysisResult);
    }

    /**
     * 批量删除预警结果
     *
     * @param ids 需要删除的预警结果主键
     * @return 结果
     */
    @Override
    public int deleteAnalysisResultByIds(Long[] ids)
    {
        return analysisResultMapper.deleteAnalysisResultByIds(ids);
    }

    /**
     * 删除预警结果信息
     * @param id 预警结果主键
     * @return 结果
     */
    @Override
    public int deleteAnalysisResultById(Long id)
    {
        return analysisResultMapper.deleteAnalysisResultById(id);
    }

    /**
     * 核心方法：根据新采集的数据，判断是否生成预警
     * @param realTimeData 最新采集的实时数据
     */
    @Override
    public void generateWarning(RealTimeData realTimeData) {
        // 1. 从数据库查询配置的预警阈值
        // 我们使用模块名 "analyzer" 和配置键来精确查找
        SystemMetadata cpuThresholdMeta = systemMetadataMapper.selectByModuleAndKey("analyzer", "cpuThreshold");
        SystemMetadata memThresholdMeta = systemMetadataMapper.selectByModuleAndKey("analyzer", "memThreshold");
        SystemMetadata diskThresholdMeta = systemMetadataMapper.selectByModuleAndKey("analyzer", "diskThreshold");

        // 健壮性检查：如果配置不存在，则不进行预警
        if (cpuThresholdMeta == null || memThresholdMeta == null || diskThresholdMeta == null) {
            // 在实际项目中，这里应该记录日志，提示管理员配置预警阈值
            return;
        }

        // 2. 将配置值（String类型）转换为BigDecimal，用于比较
        BigDecimal cpuThreshold = new BigDecimal(cpuThresholdMeta.getConfigValue());
        BigDecimal memThreshold = new BigDecimal(memThresholdMeta.getConfigValue());
        BigDecimal diskThreshold = new BigDecimal(diskThresholdMeta.getConfigValue());

        // 3. 获取新采集的数据值
        BigDecimal currentCpu = realTimeData.getCpuUsage();
        BigDecimal currentMem = realTimeData.getMemUsage();
        BigDecimal currentDisk = realTimeData.getDiskUsage();

        // 4. 比较判断，如果超过阈值，则创建预警记录
        // 使用 compareTo 方法进行BigDecimal比较，>0表示大于，==0表示等于，<0表示小于
        if (currentCpu.compareTo(cpuThreshold) > 0) {
            createWarningRecord(realTimeData.getId(), "CPU过高");
        }
        if (currentMem.compareTo(memThreshold) > 0) {
            createWarningRecord(realTimeData.getId(), "内存过高");
        }
        if (currentDisk.compareTo(diskThreshold) > 0) {
            createWarningRecord(realTimeData.getId(), "磁盘过高");
        }
    }

    /**
     * 辅助方法：创建并插入一条预警记录到数据库
     * @param dataId 关联的实时数据ID
     * @param warningType 预警类型（如 "CPU过高"）
     */
    private void createWarningRecord(Long dataId, String warningType) {
        AnalysisResult warning = new AnalysisResult();
        warning.setDataId(dataId);          // 关联的实时数据ID
        warning.setWarningType(warningType); // 预警类型
        warning.setWarningLevel("基础预警"); // 预警级别，目前只有一种
        warning.setIsHandled(0);            // 处理状态，0表示未处理

        // 调用本类的insert方法，该方法会自动设置分析时间
        this.insertAnalysisResult(warning);
    }

    /**
     * 新增：查询预警数量（用于前端统计）
     * @param isHandled 处理状态 (0:未处理, 1:已处理, null:全部)
     * @return 预警数量
     */
    @Override
    public int selectWarningCount(Integer isHandled) {
        return analysisResultMapper.selectWarningCount(isHandled);
    }
}