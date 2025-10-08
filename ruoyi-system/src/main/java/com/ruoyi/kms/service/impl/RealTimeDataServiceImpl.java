package com.ruoyi.kms.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.kms.domain.AnalysisResult;
import com.ruoyi.kms.domain.RealTimeData;
import com.ruoyi.kms.mapper.AnalysisResultMapper;
import com.ruoyi.kms.mapper.RealTimeDataMapper;
import com.ruoyi.kms.service.IRealTimeDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 关键指标实时数据Service（仅负责实时数据CRUD，无采集/预警逻辑）
 *
 * @author hby
 * @date 2025-09-28
 */
@Slf4j
@Service
public class RealTimeDataServiceImpl implements IRealTimeDataService {
    @Autowired
    private RealTimeDataMapper realTimeDataMapper;

    // JSON工具：用于校验磁盘数据格式
    @Autowired
    private ObjectMapper objectMapper;

    private AnalysisResultMapper analysisResultMapper;

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
        if (diskDataJson == null || diskDataJson.trim().isEmpty()) {
            throw new ServiceException("磁盘JSON数据不能为空");
        }

        try {
            // 1. 将JSON字符串解析成一个Map对象的列表
            // 使用TypeReference来精确指定泛型类型
            List<Map<String, Object>> diskList = objectMapper.readValue(
                    diskDataJson,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // 2. 检查磁盘列表是否为空
            if (diskList == null || diskList.isEmpty()) {
                throw new ServiceException("磁盘JSON数组为空，至少需要一条磁盘数据");
            }

            // 3. 遍历列表，校验每个磁盘对象的核心字段
            for (int i = 0; i < diskList.size(); i++) {
                Map<String, Object> diskMap = diskList.get(i);
                if (diskMap == null) {
                    throw new ServiceException("磁盘JSON数组中第 " + (i + 1) + " 个元素为null");
                }

                if (!diskMap.containsKey("path") || diskMap.get("path") == null || diskMap.get("path").toString().trim().isEmpty()) {
                    throw new ServiceException("磁盘JSON数组中第 " + (i + 1) + " 个元素缺少核心字段：path（磁盘路径）");
                }
                if (!diskMap.containsKey("type") || diskMap.get("type") == null || diskMap.get("type").toString().trim().isEmpty()) {
                    throw new ServiceException("磁盘JSON数组中第 " + (i + 1) + " 个元素缺少核心字段：type（磁盘类型）");
                }
                if (!diskMap.containsKey("total") || diskMap.get("total") == null) {
                    throw new ServiceException("磁盘JSON数组中第 " + (i + 1) + " 个元素缺少核心字段：total（总大小）");
                }
                if (!diskMap.containsKey("usage") || diskMap.get("usage") == null) {
                    throw new ServiceException("磁盘JSON数组中第 " + (i + 1) + " 个元素缺少核心字段：usage（使用率）");
                }
            }

        } catch (ServiceException e) {
            // 如果是我们自己抛出的异常，直接重新抛出
            throw e;
        } catch (Exception e) {
            // 捕获所有其他解析或处理异常
            throw new ServiceException("磁盘JSON格式或内容校验失败：" + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> selectRealTimeDataWithWarning(RealTimeData realTimeData) {
        List<RealTimeData> dataList = realTimeDataMapper.selectRealTimeDataList(realTimeData);

        // 处理 dataList 为 null 的情况（若Mapper返回null，避免后续流处理报错）
        if (dataList == null) {
            dataList = new ArrayList<>();
        }

        return dataList.stream().map(data -> {
            Map<String, Object> map = new HashMap<>();
            // 1. 先判断 data 是否为 null（避免 data.getId() 报错）
            if (data == null) {
                log.warn("实时数据列表中存在null元素，跳过处理");
                return map;
            }

            // 2. 复制实时数据字段（正常逻辑）
            map.put("id", data.getId());
            map.put("cpuUsage", data.getCpuUsage());
            map.put("memUsage", data.getMemUsage());
            map.put("diskData", data.getDiskData());
            map.put("collectTime", data.getCollectTime());
            map.put("isValid", data.getIsValid());

            // 3. 查询预警记录：关键！判断返回结果是否为null，若为null则赋值为空列表
            AnalysisResult query = new AnalysisResult();
            query.setDataId(data.getId());
            List<AnalysisResult> warningList = analysisResultMapper.selectAnalysisResultList(query);
            // 核心修复：若 warningList 为 null，转为空列表
            warningList = warningList == null ? new ArrayList<>() : warningList;

            // 4. 计算预警状态和数量（此时 warningList 不可能为 null，可安全调用流方法）
            int warningCount = warningList.size();
            // 流处理前无需再判断，因 warningList 已确保非null
            boolean hasUnhandled = warningList.stream()
                    .anyMatch(w -> {
                        // 额外判断：避免 w 为 null（若预警列表中存在null元素）
                        return w != null && w.getIsHandled() == 0;
                    });
            int warningStatus = hasUnhandled ? 1 : (warningCount > 0 ? 2 : 0);

            // 5. 添加预警字段
            map.put("warningCount", warningCount);
            map.put("warningStatus", warningStatus);

            return map;
        }).collect(Collectors.toList());
    }
}