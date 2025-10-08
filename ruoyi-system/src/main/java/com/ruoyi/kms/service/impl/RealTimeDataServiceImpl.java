package com.ruoyi.kms.service.impl;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
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

    // 用于 JSON 格式校验和转换的工具类（Spring 自带，无需额外依赖）
    @Autowired
    private ObjectMapper objectMapper;

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
        // 1. 处理磁盘数据：若前端传递的是字段数组，先转为 JSON 字符串（可选逻辑）
        // （注：若前端直接传 JSON 字符串，可跳过此步，直接校验）
        String diskData = realTimeData.getDiskData();
        if (diskData == null || diskData.isEmpty()) {
            // 示例：若前端传递的是 Map 格式（如 {"usage":80.5,"path":"C:/"}），可手动转为 JSON
            // 此处需根据前端实际传参调整，若前端直接传 JSON 则无需此逻辑
            throw new ServiceException("磁盘数据（diskData）不能为空");
        }

        // 2. 校验 diskData 是否为合法 JSON（避免存入非法格式）
        if (!isValidJson(diskData)) {
            throw new ServiceException("磁盘数据（diskData）不是合法的 JSON 格式");
        }

        // 3. 校验 JSON 中是否包含磁盘核心字段（usage、path、total 等）
        if (!hasRequiredDiskFields(diskData)) {
            throw new ServiceException("磁盘 JSON 数据需包含：使用率（usage）、路径（path）、总大小（total）");
        }

        // 4. 插入数据库（直接存储 JSON 字符串）
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

    /**
     * 辅助方法：校验字符串是否为合法 JSON
     */
    private boolean isValidJson(String jsonStr) {
        try {
            objectMapper.readTree(jsonStr); // 尝试解析 JSON，失败则抛异常
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 辅助方法：校验 JSON 中是否包含磁盘核心字段
     */
    private boolean hasRequiredDiskFields(String jsonStr) {
        try {
            Map<String, Object> diskMap = objectMapper.readValue(jsonStr, Map.class);
            // 校验核心字段是否存在（可根据业务调整）
            return diskMap.containsKey("usage")
                    && diskMap.containsKey("path")
                    && diskMap.containsKey("total");
        } catch (Exception e) {
            return false;
        }
    }
}
