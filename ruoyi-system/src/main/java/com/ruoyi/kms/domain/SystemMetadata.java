package com.ruoyi.kms.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 系统元数据对象 system_metadata
 * 
 * @author hby
 * @date 2025-09-28
 */
public class SystemMetadata extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 自增主键 */
    private Long id;

    /** 模块名称（如collector/analyzer） */
    @Excel(name = "模块名称", readConverterExp = "如collector/analyzer")
    private String moduleName;

    /** 配置项键名 */
    @Excel(name = "配置项键名")
    private String configKey;

    /** 配置项值 */
    @Excel(name = "配置项值")
    private String configValue;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setModuleName(String moduleName) 
    {
        this.moduleName = moduleName;
    }

    public String getModuleName() 
    {
        return moduleName;
    }

    public void setConfigKey(String configKey) 
    {
        this.configKey = configKey;
    }

    public String getConfigKey() 
    {
        return configKey;
    }

    public void setConfigValue(String configValue) 
    {
        this.configValue = configValue;
    }

    public String getConfigValue() 
    {
        return configValue;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("moduleName", getModuleName())
            .append("configKey", getConfigKey())
            .append("configValue", getConfigValue())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
