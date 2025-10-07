package com.ruoyi.kms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.kms.mapper.RealTimeDataMapper;
import com.ruoyi.kms.domain.RealTimeData;
import com.ruoyi.kms.service.IRealTimeDataService;

/**
 * 关键指标实时数据Service业务层处理
 * 
 * @author hby
 * @date 2025-09-28
 */
@Service
public class RealTimeDataServiceImpl implements IRealTimeDataService 
{
    @Autowired
    private RealTimeDataMapper realTimeDataMapper;

    /**
     * 查询关键指标实时数据
     * 
     * @param id 关键指标实时数据主键
     * @return 关键指标实时数据
     */
    @Override
    public RealTimeData selectRealTimeDataById(Long id)
    {
        return realTimeDataMapper.selectRealTimeDataById(id);
    }

    /**
     * 查询关键指标实时数据列表
     * 
     * @param realTimeData 关键指标实时数据
     * @return 关键指标实时数据
     */
    @Override
    public List<RealTimeData> selectRealTimeDataList(RealTimeData realTimeData)
    {
        return realTimeDataMapper.selectRealTimeDataList(realTimeData);
    }

    /**
     * 新增关键指标实时数据
     * 
     * @param realTimeData 关键指标实时数据
     * @return 结果
     */
    @Override
    public int insertRealTimeData(RealTimeData realTimeData)
    {
        return realTimeDataMapper.insertRealTimeData(realTimeData);
    }

    /**
     * 修改关键指标实时数据
     * 
     * @param realTimeData 关键指标实时数据
     * @return 结果
     */
    @Override
    public int updateRealTimeData(RealTimeData realTimeData)
    {
        return realTimeDataMapper.updateRealTimeData(realTimeData);
    }

    /**
     * 批量删除关键指标实时数据
     * 
     * @param ids 需要删除的关键指标实时数据主键
     * @return 结果
     */
    @Override
    public int deleteRealTimeDataByIds(Long[] ids)
    {
        return realTimeDataMapper.deleteRealTimeDataByIds(ids);
    }

    /**
     * 删除关键指标实时数据信息
     * 
     * @param id 关键指标实时数据主键
     * @return 结果
     */
    @Override
    public int deleteRealTimeDataById(Long id)
    {
        return realTimeDataMapper.deleteRealTimeDataById(id);
    }
    /**
     * 获取最新的一条关键指标实时数据
     *
     * @return 关键指标实时数据
     */
    @Override
    public RealTimeData selectLatestData() { return realTimeDataMapper.selectLatestData();}
}
