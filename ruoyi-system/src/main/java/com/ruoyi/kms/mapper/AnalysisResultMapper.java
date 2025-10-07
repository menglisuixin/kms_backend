package com.ruoyi.kms.mapper;

import java.util.List;
import com.ruoyi.kms.domain.AnalysisResult;
import com.ruoyi.kms.domain.SystemMetadata;

/**
 * 预警结果，关联实时数据，主删除从同步删除Mapper接口
 * 
 * @author hby
 * @date 2025-09-27
 */
public interface AnalysisResultMapper 
{
    /**
     * 查询预警结果，关联实时数据，主删除从同步删除
     * 
     * @param id 预警结果，关联实时数据，主删除从同步删除主键
     * @return 预警结果，关联实时数据，主删除从同步删除
     */
    public AnalysisResult selectAnalysisResultById(Long id);

    /**
     * 查询预警结果，关联实时数据，主删除从同步删除列表
     * 
     * @param analysisResult 预警结果，关联实时数据，主删除从同步删除
     * @return 预警结果，关联实时数据，主删除从同步删除集合
     */
    public List<AnalysisResult> selectAnalysisResultList(AnalysisResult analysisResult);

    /**
     * 新增预警结果，关联实时数据，主删除从同步删除
     * 
     * @param analysisResult 预警结果，关联实时数据，主删除从同步删除
     * @return 结果
     */
    public int insertAnalysisResult(AnalysisResult analysisResult);

    /**
     * 修改预警结果，关联实时数据，主删除从同步删除
     * 
     * @param analysisResult 预警结果，关联实时数据，主删除从同步删除
     * @return 结果
     */
    public int updateAnalysisResult(AnalysisResult analysisResult);

    /**
     * 删除预警结果，关联实时数据，主删除从同步删除
     * 
     * @param id 预警结果，关联实时数据，主删除从同步删除主键
     * @return 结果
     */
    public int deleteAnalysisResultById(Long id);

    /**
     * 批量删除预警结果，关联实时数据，主删除从同步删除
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAnalysisResultByIds(Long[] ids);
    /**
     * 根据处理状态查询预警数量
     * @param isHandled 处理状态 (0:未处理, 1:已处理, null:查询全部)
     * @return 预警数量
     */
    public int selectWarningCount(Integer isHandled);
}
