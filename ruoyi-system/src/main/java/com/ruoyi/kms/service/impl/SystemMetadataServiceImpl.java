package com.ruoyi.kms.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.kms.mapper.SystemMetadataMapper;
import com.ruoyi.kms.domain.SystemMetadata;
import com.ruoyi.kms.service.ISystemMetadataService;

/**
 * 系统元数据Service业务层处理
 * 
 * @author hby
 * @date 2025-09-28
 */
@Service
public class SystemMetadataServiceImpl implements ISystemMetadataService 
{
    @Autowired
    private SystemMetadataMapper systemMetadataMapper;

    /**
     * 查询系统元数据
     * 
     * @param id 系统元数据主键
     * @return 系统元数据
     */
    @Override
    public SystemMetadata selectSystemMetadataById(Long id)
    {
        return systemMetadataMapper.selectSystemMetadataById(id);
    }

    /**
     * 查询系统元数据列表
     * 
     * @param systemMetadata 系统元数据
     * @return 系统元数据
     */
    @Override
    public List<SystemMetadata> selectSystemMetadataList(SystemMetadata systemMetadata)
    {
        return systemMetadataMapper.selectSystemMetadataList(systemMetadata);
    }

    /**
     * 新增系统元数据
     * 
     * @param systemMetadata 系统元数据
     * @return 结果
     */
    @Override
    public int insertSystemMetadata(SystemMetadata systemMetadata)
    {
        return systemMetadataMapper.insertSystemMetadata(systemMetadata);
    }

    /**
     * 修改系统元数据
     * 
     * @param systemMetadata 系统元数据
     * @return 结果
     */
    @Override
    public int updateSystemMetadata(SystemMetadata systemMetadata)
    {
        systemMetadata.setUpdateTime(DateUtils.getNowDate());
        return systemMetadataMapper.updateSystemMetadata(systemMetadata);
    }

    /**
     * 批量删除系统元数据
     * 
     * @param ids 需要删除的系统元数据主键
     * @return 结果
     */
    @Override
    public int deleteSystemMetadataByIds(Long[] ids)
    {
        return systemMetadataMapper.deleteSystemMetadataByIds(ids);
    }

    /**
     * 删除系统元数据信息
     * 
     * @param id 系统元数据主键
     * @return 结果
     */
    @Override
    public int deleteSystemMetadataById(Long id)
    {
        return systemMetadataMapper.deleteSystemMetadataById(id);
    }
}
