package com.ruoyi.kms.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.kms.domain.SystemMetadata;
import com.ruoyi.kms.mapper.SystemMetadataMapper;
import com.ruoyi.kms.service.ISystemMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统元数据Service（仅负责元数据CRUD，无采集/预警逻辑）
 *
 * @author hby
 * @date 2025-09-28
 */
@Service
public class SystemMetadataServiceImpl implements ISystemMetadataService {
    @Autowired
    private SystemMetadataMapper systemMetadataMapper;

    @Override
    public SystemMetadata selectSystemMetadataById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("查询ID不能为空");
        }
        return systemMetadataMapper.selectSystemMetadataById(id);
    }

    @Override
    public List<SystemMetadata> selectSystemMetadataList(SystemMetadata systemMetadata) {
        return systemMetadataMapper.selectSystemMetadataList(systemMetadata);
    }

    @Override
    public int insertSystemMetadata(SystemMetadata systemMetadata) {
        // 校验核心字段（模块名+配置键唯一）
        if (systemMetadata.getModuleName() == null || systemMetadata.getModuleName().isEmpty()) {
            throw new IllegalArgumentException("模块名（moduleName）不能为空");
        }
        if (systemMetadata.getConfigKey() == null || systemMetadata.getConfigKey().isEmpty()) {
            throw new IllegalArgumentException("配置键（configKey）不能为空");
        }
        // 校验是否已存在相同模块+配置键的记录
        SystemMetadata exist = selectByModuleAndKey(systemMetadata.getModuleName(), systemMetadata.getConfigKey());
        if (exist != null) {
            throw new IllegalArgumentException("已存在模块[" + systemMetadata.getModuleName() + "]-配置键[" + systemMetadata.getConfigKey() + "]的记录");
        }
        // 设置默认更新时间
        if (systemMetadata.getUpdateTime() == null) {
            systemMetadata.setUpdateTime(DateUtils.getNowDate());
        }
        return systemMetadataMapper.insertSystemMetadata(systemMetadata);
    }

    @Override
    public int updateSystemMetadata(SystemMetadata systemMetadata) {
        if (systemMetadata.getId() == null) {
            throw new IllegalArgumentException("修改ID不能为空");
        }
        // 强制更新时间为当前时间
        systemMetadata.setUpdateTime(DateUtils.getNowDate());
        return systemMetadataMapper.updateSystemMetadata(systemMetadata);
    }

    @Override
    public int deleteSystemMetadataByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new IllegalArgumentException("删除ID列表不能为空");
        }
        return systemMetadataMapper.deleteSystemMetadataByIds(ids);
    }

    @Override
    public int deleteSystemMetadataById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("删除ID不能为空");
        }
        return systemMetadataMapper.deleteSystemMetadataById(id);
    }

    /**
     * 根据模块名+配置键查询元数据（给其他服务调用，如后续预警阈值从元数据读取）
     */
    @Override
    public SystemMetadata selectByModuleAndKey(String moduleName, String configKey) {
        if (moduleName == null || moduleName.isEmpty() || configKey == null || configKey.isEmpty()) {
            throw new IllegalArgumentException("模块名和配置键不能为空");
        }
        return systemMetadataMapper.selectByModuleAndKey(moduleName, configKey);
    }
}