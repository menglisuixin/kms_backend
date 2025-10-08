package com.ruoyi.kms.service;

import com.ruoyi.kms.domain.SystemMetadata;
import java.util.List;

/**
 * 系统元数据Service接口
 *
 * @author hby
 * @date 2025-09-28
 */
public interface ISystemMetadataService
{
    /**
     * 查询系统元数据
     */
    public SystemMetadata selectSystemMetadataById(Long id);

    /**
     * 查询系统元数据列表
     */
    public List<SystemMetadata> selectSystemMetadataList(SystemMetadata systemMetadata);

    /**
     * 新增系统元数据
     */
    public int insertSystemMetadata(SystemMetadata systemMetadata);

    /**
     * 修改系统元数据
     */
    public int updateSystemMetadata(SystemMetadata systemMetadata);

    /**
     * 批量删除系统元数据
     */
    public int deleteSystemMetadataByIds(Long[] ids);

    /**
     * 删除系统元数据信息
     */
    public int deleteSystemMetadataById(Long id);

    /**
     * 根据模块和键查询系统元数据
     */
    public SystemMetadata selectByModuleAndKey(String moduleName, String configKey);
}
