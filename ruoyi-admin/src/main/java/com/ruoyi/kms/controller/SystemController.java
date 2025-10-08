package com.ruoyi.kms.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.kms.domain.SystemMetadata;
import com.ruoyi.kms.service.ISystemMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/kms/system")
public class SystemController extends BaseController {
    @Autowired
    private ISystemMetadataService metadataService;

    // 获取系统状态（简化：默认运行正常）
    @PreAuthorize("@ss.hasPermi('kms:system:view')") // 权限校验
    @GetMapping("/status")
    public AjaxResult getSystemStatus() {
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("collector", "运行中");
        statusMap.put("analyzer", "运行中");
        statusMap.put("database", "连接正常");
        statusMap.put("backend", "运行中");
        return success(statusMap);
    }

//    // 获取阈值配置
//    @PreAuthorize("@ss.hasPermi('kms:system:view')") // 权限校验
//    @GetMapping("/config")
//    public AjaxResult getSystemConfig() {
//        SystemMetadata metadata = new SystemMetadata();
//        metadata.setModuleName("analyzer");
//        List<SystemMetadata> list = metadataService.selectSystemMetadataList(metadata);
//        return success(list);
//    }

//    // 获取阈值配置（返回区间格式）
//    @PreAuthorize("@ss.hasPermi('kms:system:view')")
//    @GetMapping("/config")
//    public AjaxResult getSystemConfig() {
//        // 1. 查询分析器模块的所有阈值配置
//        SystemMetadata metadata = new SystemMetadata();
//        metadata.setModuleName("analyzer");
//        List<SystemMetadata> originalList = metadataService.selectSystemMetadataList(metadata);
//
//        // 2. 将原始配置转换为Map便于查找
//        Map<String, String> configMap = new HashMap<>();
//        for (SystemMetadata item : originalList) {
//            configMap.put(item.getConfigKey(), item.getConfigValue());
//        }
//
//        // 3. 定义四个预警区间（与AnalysisResultServiceImpl中的阈值对应）
//        List<Map<String, Object>> thresholdRanges = new ArrayList<>();
//
//        // 添加CPU阈值区间
//        thresholdRanges.add(createRangeMap(
//                "CPU使用率",
//                "0-60",
//                "无预警",
//                configMap.get("cpuThresholdLevel1") // 60
//        ));
//        thresholdRanges.add(createRangeMap(
//                "CPU使用率",
//                "60-80",
//                "基础预警",
//                configMap.get("cpuThresholdLevel2") // 80
//        ));
//        thresholdRanges.add(createRangeMap(
//                "CPU使用率",
//                "80-90",
//                "中级预警",
//                configMap.get("cpuThresholdLevel3") // 90
//        ));
//        thresholdRanges.add(createRangeMap(
//                "CPU使用率",
//                "90-100",
//                "严重预警",
//                "100"
//        ));
//
//        // 添加内存阈值区间
//        thresholdRanges.add(createRangeMap(
//                "内存使用率",
//                "0-60",
//                "无预警",
//                configMap.get("memThresholdLevel1") // 60
//        ));
//        thresholdRanges.add(createRangeMap(
//                "内存使用率",
//                "60-80",
//                "基础预警",
//                configMap.get("memThresholdLevel2") // 80
//        ));
//        thresholdRanges.add(createRangeMap(
//                "内存使用率",
//                "80-90",
//                "中级预警",
//                configMap.get("memThresholdLevel3") // 90
//        ));
//        thresholdRanges.add(createRangeMap(
//                "内存使用率",
//                "90-100",
//                "严重预警",
//                "100"
//        ));
//
//        // 添加磁盘阈值区间
//        thresholdRanges.add(createRangeMap(
//                "磁盘使用率",
//                "0-60",
//                "无预警",
//                configMap.get("diskThresholdLevel1") // 60
//        ));
//        thresholdRanges.add(createRangeMap(
//                "磁盘使用率",
//                "60-80",
//                "基础预警",
//                configMap.get("diskThresholdLevel2") // 80
//        ));
//        thresholdRanges.add(createRangeMap(
//                "磁盘使用率",
//                "80-90",
//                "中级预警",
//                configMap.get("diskThresholdLevel3") // 90
//        ));
//        thresholdRanges.add(createRangeMap(
//                "磁盘使用率",
//                "90-100",
//                "严重预警",
//                "100"
//        ));
//
//        return success(thresholdRanges);
//    }

    // 获取阈值配置（返回区间格式）
    @PreAuthorize("@ss.hasPermi('kms:system:view')")
    @GetMapping("/config")
    public AjaxResult getSystemConfig() {
        // 查询分析器模块的所有阈值配置
        SystemMetadata query = new SystemMetadata();
        query.setModuleName("analyzer");
        List<SystemMetadata> metadataList = metadataService.selectSystemMetadataList(query);

        List<Map<String, Object>> thresholdRanges = new ArrayList<>();

        // 处理每个资源的阈值区间
        for (SystemMetadata metadata : metadataList) {
            String resourceName = getResourceName(metadata.getConfigKey());
            if (resourceName == null) {
                continue;
            }

            // 添加四个区间
            thresholdRanges.add(createRangeMap(
                    resourceName,
                    "0-" + metadata.getWarningLevel1Value(),
                    "无预警",
                    metadata.getWarningLevel1Value()
            ));
            thresholdRanges.add(createRangeMap(
                    resourceName,
                    metadata.getWarningLevel1Value() + "-" + metadata.getWarningLevel2Value(),
                    "基础预警",
                    metadata.getWarningLevel2Value()
            ));
            thresholdRanges.add(createRangeMap(
                    resourceName,
                    metadata.getWarningLevel2Value() + "-" + metadata.getWarningLevel3Value(),
                    "中级预警",
                    metadata.getWarningLevel3Value()
            ));
            thresholdRanges.add(createRangeMap(
                    resourceName,
                    metadata.getWarningLevel3Value() + "-100",
                    "严重预警",
                    "100"
            ));
        }

        return success(thresholdRanges);
    }

    /**
     * 将配置键转换为资源名称
     */
    private String getResourceName(String configKey) {
        switch (configKey) {
            case "cpuThreshold": return "CPU使用率";
            case "memThreshold": return "内存使用率";
            case "diskThreshold": return "磁盘使用率";
            default: return null;
        }
    }

    /**
     * 创建区间信息Map
     */
    private Map<String, Object> createRangeMap(String resource, String range, String warningLevel, String threshold) {
        Map<String, Object> rangeMap = new HashMap<>();
        rangeMap.put("resource", resource);
        rangeMap.put("range", range);
        rangeMap.put("warningLevel", warningLevel);
        rangeMap.put("threshold", threshold);
        return rangeMap;
    }
}