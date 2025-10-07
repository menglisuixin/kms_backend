package com.ruoyi.kms.mapper;

import java.util.List;
import com.ruoyi.kms.domain.SystemMetadata;
import org.apache.ibatis.annotations.Param;

/**
 * 系统元数据Mapper接口
 * 
 * @author hby
 * @date 2025-09-28
 */
public interface SystemMetadataMapper 
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
     * 删除系统元数据
     * 
     * @param id 系统元数据主键
     * @return 结果
     */
    public int deleteSystemMetadataById(Long id);

    /**
     * 批量删除系统元数据
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSystemMetadataByIds(Long[] ids);
    /**
     * 根据模块名和配置键查询唯一的配置项
     * @param moduleName 模块名
     * @param configKey 配置键
     * @return 系统元数据对象，如果不存在则返回null
     */
    public SystemMetadata selectByModuleAndKey(@Param("moduleName") String moduleName, @Param("configKey") String configKey);
}
