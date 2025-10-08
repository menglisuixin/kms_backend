package com.ruoyi.kms.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.kms.domain.RealTimeData;
import com.ruoyi.kms.mapper.RealTimeDataMapper;
import com.ruoyi.kms.service.IRealTimeDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 关键指标实时数据Service（仅负责实时数据CRUD，无采集/预警逻辑）
 *
 * @author hby
 * @date 2025-09-28
 */
@Service
public class RealTimeDataServiceImpl implements IRealTimeDataService {
    @Autowired
    private RealTimeDataMapper realTimeDataMapper;

    // JSON工具：用于校验磁盘数据格式
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public RealTimeData selectRealTimeDataById(Long id) {
        if (id == null) {
            throw new ServiceException("查询ID不能为空");
        }
        return realTimeDataMapper.selectRealTimeDataById(id);
    }

    @Override
    public List<RealTimeData> selectRealTimeDataList(RealTimeData realTimeData) {
        return realTimeDataMapper.selectRealTimeDataList(realTimeData);
    }

    @Override
    public int insertRealTimeData(RealTimeData realTimeData) {
        // 1. 校验核心字段（非空）
        validateRealTimeData(realTimeData);

        // 2. 校验磁盘数据JSON合法性
        validateDiskData(realTimeData.getDiskData());

        // 3. 插入数据库
        return realTimeDataMapper.insertRealTimeData(realTimeData);
    }

    @Override
    public int updateRealTimeData(RealTimeData realTimeData) {
        if (realTimeData.getId() == null) {
            throw new ServiceException("修改ID不能为空");
        }
        // 若修改磁盘数据，需重新校验JSON格式
        if (realTimeData.getDiskData() != null && !realTimeData.getDiskData().isEmpty()) {
            validateDiskData(realTimeData.getDiskData());
        }
        return realTimeDataMapper.updateRealTimeData(realTimeData);
    }

    @Override
    public int deleteRealTimeDataByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new ServiceException("删除ID列表不能为空");
        }
        return realTimeDataMapper.deleteRealTimeDataByIds(ids);
    }

    @Override
    public int deleteRealTimeDataById(Long id) {
        if (id == null) {
            throw new ServiceException("删除ID不能为空");
        }
        return realTimeDataMapper.deleteRealTimeDataById(id);
    }

    @Override
    public RealTimeData selectLatestData() {
        return realTimeDataMapper.selectLatestData();
    }

    /**
     * 校验实时数据核心字段（非空）
     */
    private void validateRealTimeData(RealTimeData data) {
        if (data.getCpuUsage() == null) {
            throw new ServiceException("CPU使用率不能为空");
        }
        if (data.getMemUsage() == null) {
            throw new ServiceException("内存使用率不能为空");
        }
        if (data.getDiskData() == null || data.getDiskData().isEmpty()) {
            throw new ServiceException("磁盘数据（diskData）不能为空");
        }
        if (data.getCollectTime() == null) {
            throw new ServiceException("采集时间不能为空");
        }
        if (data.getIsValid() == null) {
            data.setIsValid(1); // 默认有效
        }
    }

    /**
     * 校验磁盘数据JSON格式（合法+包含核心字段）
     */
    private void validateDiskData(String diskDataJson) {
        // 1. 校验JSON格式合法性
        try {
            objectMapper.readTree(diskDataJson);
        } catch (Exception e) {
            throw new ServiceException("磁盘数据不是合法JSON格式：" + e.getMessage());
        }

        // 2. 校验JSON包含核心字段（path/type/total/usage）
        try {
            Map<String, Object> diskMap = objectMapper.readValue(diskDataJson, Map.class);
            if (!diskMap.containsKey("path") || diskMap.get("path") == null) {
                throw new ServiceException("磁盘JSON缺少核心字段：path（磁盘路径）");
            }
            if (!diskMap.containsKey("type") || diskMap.get("type") == null) {
                throw new ServiceException("磁盘JSON缺少核心字段：type（磁盘类型）");
            }
            if (!diskMap.containsKey("total") || diskMap.get("total") == null) {
                throw new ServiceException("磁盘JSON缺少核心字段：total（总大小）");
            }
            if (!diskMap.containsKey("usage") || diskMap.get("usage") == null) {
                throw new ServiceException("磁盘JSON缺少核心字段：usage（使用率）");
            }
        } catch (Exception e) {
            throw new ServiceException("磁盘JSON核心字段校验失败：" + e.getMessage());
        }
    }
}