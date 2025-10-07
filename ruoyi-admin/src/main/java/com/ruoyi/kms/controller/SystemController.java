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

    // 获取阈值配置
    @PreAuthorize("@ss.hasPermi('kms:system:view')") // 权限校验
    @GetMapping("/config")
    public AjaxResult getSystemConfig() {
        SystemMetadata metadata = new SystemMetadata();
        metadata.setModuleName("analyzer");
        List<SystemMetadata> list = metadataService.selectSystemMetadataList(metadata);
        return success(list);
    }
}