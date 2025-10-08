package com.ruoyi.kms.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.kms.domain.AnalysisResult;
import com.ruoyi.kms.domain.RealTimeData;
import com.ruoyi.kms.domain.server.KmsSysFile;
import com.ruoyi.kms.mapper.AnalysisResultMapper;
import com.ruoyi.kms.mapper.RealTimeDataMapper;
import com.ruoyi.kms.service.IAnalysisResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预警结果Service业务层处理
 *
 * @author hby
 * @date 2025-09-27
 */
@Service
public class AnalysisResultServiceImpl implements IAnalysisResultService {
    private static final Logger log = LoggerFactory.getLogger(AnalysisResultServiceImpl.class);

    @Autowired
    private AnalysisResultMapper analysisResultMapper;

    @Autowired
    private RealTimeDataMapper realTimeDataMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // 预警阈值配置
    private static final BigDecimal LEVEL1_THRESHOLD = new BigDecimal("60");   // 基础预警
    private static final BigDecimal LEVEL2_THRESHOLD = new BigDecimal("80");   // 中级预警
    private static final BigDecimal LEVEL3_THRESHOLD = new BigDecimal("90");   // 严重预警
    private static final BigDecimal MAX_THRESHOLD = new BigDecimal("100");     // 最大使用率


//    public void generateWarning(RealTimeData realTimeData) {
//        if (realTimeData == null) {
//            log.error("生成预警失败：实时数据为空");
//            return;
//        }
//
//        // CPU预警判断
//        checkAndCreateWarning(realTimeData.getId(), "CPU过高", realTimeData.getCpuUsage());
//
//        // 内存预警判断
//        checkAndCreateWarning(realTimeData.getId(), "内存过高", realTimeData.getMemUsage());
//
//        // 磁盘预警判断（从JSON解析使用率）
//        BigDecimal diskUsage = parseDiskUsage(realTimeData.getDiskData());
//        checkAndCreateWarning(realTimeData.getId(), "磁盘过高", diskUsage);
//    }
    /**
     * 核心方法：根据实时数据生成预警
     */
    @Override
    public void generateWarning(RealTimeData data, KmsSysFile disk) {
        try {
            if (data == null || disk == null) {
                log.error("生成预警失败：实时数据或磁盘信息为空");
                return;
            }
            Long dataId = data.getId(); // 关联的实时数据ID

            // 1. CPU预警：调用checkAndCreateWarning保存记录
            checkAndCreateWarning(
                    dataId,
                    "CPU使用率过高",
                    data.getCpuUsage()
            );

            // 2. 内存预警：同理保存
            checkAndCreateWarning(
                    dataId,
                    "内存使用率过高",
                    data.getMemUsage()
            );

            // 3. 磁盘预警：将double转为BigDecimal，适配方法参数
            BigDecimal diskUsage = BigDecimal.valueOf(disk.getUsage());
            checkAndCreateWarning(
                    dataId,
                    "磁盘[" + disk.getDirName() + "]使用率过高",
                    diskUsage
            );

            // 4. 综合预警（CPU+磁盘同时高负载）：直接创建记录
            if (disk.getUsage() > 85.0 && data.getCpuUsage().compareTo(new BigDecimal("80")) > 0) {
                createWarningRecord(
                        dataId,
                        "综合预警：CPU+磁盘高负载",
                        "严重预警"
                );
            }

        } catch (Exception e) {
            log.error("磁盘[{}]预警处理失败", disk.getDirName(), e);
        }
    }

    // RealTimeDataServiceImpl 中新增方法
    public List<Map<String, Object>> selectRealTimeDataWithWarning(RealTimeData realTimeData) {
        // 1. 查询实时数据列表
        List<RealTimeData> dataList = realTimeDataMapper.selectRealTimeDataList(realTimeData);

        // 2. 转换为Map并添加预警状态字段
        return dataList.stream().map(data -> {
            Map<String, Object> map = new HashMap<>();
            // 复制实时数据字段（使用BeanUtils或手动put）
            map.put("id", data.getId());
            map.put("cpuUsage", data.getCpuUsage());
            map.put("memUsage", data.getMemUsage());
            map.put("diskData", data.getDiskData());
            map.put("collectTime", data.getCollectTime());

            // 3. 查询该实时数据关联的预警记录
            AnalysisResult query = new AnalysisResult();
            query.setDataId(data.getId());
            List<AnalysisResult> warnings = analysisResultMapper.selectAnalysisResultList(query);

            // 4. 计算预警状态（0=无预警，1=有未处理，2=已处理）
            long unhandled = warnings.stream().filter(w -> w.getIsHandled() == 0).count();
            int warningStatus = unhandled > 0 ? 1 : (warnings.size() > 0 ? 2 : 0);

            // 5. 添加预警扩展字段
            map.put("warningStatus", warningStatus);
            map.put("warningCount", warnings.size());

            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 从JSON解析磁盘使用率
     */
    private BigDecimal parseDiskUsage(String diskDataJson) {
        if (diskDataJson == null || diskDataJson.isEmpty()) {
            log.warn("磁盘数据JSON为空，无法解析使用率");
            return null;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(diskDataJson);
            if (jsonNode.has("usage")) {
                return jsonNode.get("usage").decimalValue();
            }
            log.warn("磁盘数据JSON中不包含'usage'字段");
            return null;
        } catch (Exception e) {
            log.error("解析磁盘数据JSON失败", e);
            return null;
        }
    }

    /**
     * 检查使用率并创建预警记录
     */
    private void checkAndCreateWarning(Long dataId, String warningType, BigDecimal usage) {
        if (usage == null || usage.compareTo(BigDecimal.ZERO) < 0 || usage.compareTo(MAX_THRESHOLD) > 0) {
            log.warn("无效的使用率数据：{}，预警类型：{}", usage, warningType);
            return;
        }

        String warningLevel = determineWarningLevel(usage);
        if (warningLevel != null) {
            createWarningRecord(dataId, warningType, warningLevel);
        }
    }

    /**
     * 确定预警级别
     */
    private String determineWarningLevel(BigDecimal usage) {
        if (usage.compareTo(LEVEL3_THRESHOLD) >= 0) {
            return "严重预警";
        } else if (usage.compareTo(LEVEL2_THRESHOLD) >= 0) {
            return "中级预警";
        } else if (usage.compareTo(LEVEL1_THRESHOLD) >= 0) {
            return "基础预警";
        }
        return null;
    }

    /**
     * 创建预警记录
     */
    private void createWarningRecord(Long dataId, String warningType, String warningLevel) {
        AnalysisResult result = new AnalysisResult();
        result.setDataId(dataId);
        result.setWarningType(warningType);
        result.setWarningLevel(warningLevel);
        result.setIsHandled(0);
        result.setAnalysisTime(DateUtils.getNowDate());

        int rows = analysisResultMapper.insertAnalysisResult(result);
        if (rows > 0) {
            log.info("创建预警记录成功：{} - {}", warningType, warningLevel);
        } else {
            log.error("创建预警记录失败：{}", warningType);
        }
    }

    /**
     * 根据ID查询预警结果
     * 正确实现接口中定义的方法
     */
    @Override
    public AnalysisResult selectAnalysisResultById(Long id) {
        if (id == null) {
            log.warn("查询预警结果时ID为空");
            return null;
        }
        return analysisResultMapper.selectAnalysisResultById(id);
    }

    /**
     * 查询预警结果列表
     */
    @Override
    public List<AnalysisResult> selectAnalysisResultList(AnalysisResult analysisResult) {
        return analysisResultMapper.selectAnalysisResultList(analysisResult);
    }

    /**
     * 新增预警结果
     */
    @Override
    public int insertAnalysisResult(AnalysisResult analysisResult) {
        if (analysisResult == null) {
            return 0;
        }
        analysisResult.setAnalysisTime(DateUtils.getNowDate());
        return analysisResultMapper.insertAnalysisResult(analysisResult);
    }

    /**
     * 修改预警结果
     */
    @Override
    public int updateAnalysisResult(AnalysisResult analysisResult) {
        return analysisResultMapper.updateAnalysisResult(analysisResult);
    }

    /**
     * 批量删除预警结果
     */
    @Override
    public int deleteAnalysisResultByIds(Long[] ids) {
        return analysisResultMapper.deleteAnalysisResultByIds(ids);
    }

    /**
     * 删除预警结果
     */
    @Override
    public int deleteAnalysisResultById(Long id) {
        return analysisResultMapper.deleteAnalysisResultById(id);
    }

    /**
     * 查询预警数量
     */
    @Override
    public int selectWarningCount(Integer isHandled) {
        return analysisResultMapper.selectWarningCount(isHandled);
    }

}
