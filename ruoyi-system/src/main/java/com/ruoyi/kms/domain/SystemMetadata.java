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

    /** 一级预警阈值 */
    @Excel(name = "一级预警阈值")
    private String warningLevel1Value;

    /** 二级预警阈值 */
    @Excel(name = "二级预警阈值")
    private String warningLevel2Value;

    /** 三级预警阈值 */
    @Excel(name = "三级预警阈值")
    private String warningLevel3Value;

    public String getWarningLevel1Value() {
        return warningLevel1Value;
    }

    public void setWarningLevel1Value(String warningLevel1Value) {
        this.warningLevel1Value = warningLevel1Value;
    }

    public String getWarningLevel2Value() {
        return warningLevel2Value;
    }

    public void setWarningLevel2Value(String warningLevel2Value) {
        this.warningLevel2Value = warningLevel2Value;
    }

    public String getWarningLevel3Value() {
        return warningLevel3Value;
    }

    public void setWarningLevel3Value(String warningLevel3Value) {
        this.warningLevel3Value = warningLevel3Value;
    }

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

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("moduleName", getModuleName())
                .append("configKey", getConfigKey())
                .append("warningLevel1Value", getWarningLevel1Value())
                .append("warningLevel2Value", getWarningLevel2Value())
                .append("warningLevel3Value", getWarningLevel3Value())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
