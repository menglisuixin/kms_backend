package com.ruoyi.kms.service;

import java.util.List;
import com.ruoyi.kms.domain.AnalysisResult;
import com.ruoyi.kms.domain.RealTimeData;
import com.ruoyi.kms.domain.server.KmsSysFile;

/**
 * 预警结果Service接口
 *
 * @author hby
 * @date 2025-09-27
 */
public interface IAnalysisResultService
{
    /**
     * 查询预警结果
     *
     * @param id 预警结果主键
     * @return 预警结果
     */
    public AnalysisResult selectAnalysisResultById(Long id);

    /**
     * 查询预警结果列表
     *
     * @param analysisResult 预警结果
     * @return 预警结果集合
     */
    public List<AnalysisResult> selectAnalysisResultList(AnalysisResult analysisResult);

    /**
     * 新增预警结果
     *
     * @param analysisResult 预警结果
     * @return 结果
     */
    public int insertAnalysisResult(AnalysisResult analysisResult);

    /**
     * 修改预警结果
     *
     * @param analysisResult 预警结果
     * @return 结果
     */
    public int updateAnalysisResult(AnalysisResult analysisResult);

    /**
     * 批量删除预警结果
     *
     * @param ids 需要删除的预警结果主键集合
     * @return 结果
     */
    public int deleteAnalysisResultByIds(Long[] ids);

    /**
     * 删除预警结果信息
     *
     * @param id 预警结果主键
     * @return 结果
     */
    public int deleteAnalysisResultById(Long id);

    /**
     * 根据新采集的数据生成预警
     * @param realTimeData 最新的实时数据
     */
    void generateWarning(RealTimeData data, KmsSysFile disk);

    /**
     * 查询预警数量
     * @param isHandled 处理状态 (0:未处理, 1:已处理, null:全部)
     * @return 预警数量
     */
    public int selectWarningCount(Integer isHandled);
}
