package com.ruoyi.kms.service;

import java.util.List;
import com.ruoyi.kms.domain.SystemMetadata;

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
     * 
     * @param id 系统元数据主键
     * @return 系统元数据
     */
    public SystemMetadata selectSystemMetadataById(Long id);

    /**
     * 查询系统元数据列表
     * 
     * @param systemMetadata 系统元数据
     * @return 系统元数据集合
     */
    public List<SystemMetadata> selectSystemMetadataList(SystemMetadata systemMetadata);

    /**
     * 新增系统元数据
     * 
     * @param systemMetadata 系统元数据
     * @return 结果
     */
    public int insertSystemMetadata(SystemMetadata systemMetadata);

    /**
     * 修改系统元数据
     * 
     * @param systemMetadata 系统元数据
     * @return 结果
     */
    public int updateSystemMetadata(SystemMetadata systemMetadata);

    /**
     * 批量删除系统元数据
     * 
     * @param ids 需要删除的系统元数据主键集合
     * @return 结果
     */
    public int deleteSystemMetadataByIds(Long[] ids);

    /**
     * 删除系统元数据信息
     * 
     * @param id 系统元数据主键
     * @return 结果
     */
    public int deleteSystemMetadataById(Long id);

}
