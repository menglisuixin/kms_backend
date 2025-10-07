package com.ruoyi.kms.mapper;

import java.util.List;
import com.ruoyi.kms.domain.RealTimeData;

/**
 * 关键指标实时数据Mapper接口
 * 
 * @author hby
 * @date 2025-09-28
 */
public interface RealTimeDataMapper 
{
    /**
     * 查询关键指标实时数据
     * 
     * @param id 关键指标实时数据主键
     * @return 关键指标实时数据
     */
    public RealTimeData selectRealTimeDataById(Long id);

    /**
     * 查询关键指标实时数据列表
     * 
     * @param realTimeData 关键指标实时数据
     * @return 关键指标实时数据集合
     */
    public List<RealTimeData> selectRealTimeDataList(RealTimeData realTimeData);

    /**
     * 新增关键指标实时数据
     * 
     * @param realTimeData 关键指标实时数据
     * @return 结果
     */
    public int insertRealTimeData(RealTimeData realTimeData);

    /**
     * 修改关键指标实时数据
     * 
     * @param realTimeData 关键指标实时数据
     * @return 结果
     */
    public int updateRealTimeData(RealTimeData realTimeData);

    /**
     * 删除关键指标实时数据
     * 
     * @param id 关键指标实时数据主键
     * @return 结果
     */
    public int deleteRealTimeDataById(Long id);

    /**
     * 批量删除关键指标实时数据
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRealTimeDataByIds(Long[] ids);
    /**
     * 获取最新的一条关键指标实时数据
     *
     * @return 关键指标实时数据
     */
    public RealTimeData selectLatestData();
}
