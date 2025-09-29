package com.ruoyi.kms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;


/**
 * 预警结果，关联实时数据，主删除从同步删除对象 analysis_result
 * 
 * @author hby
 * @date 2025-09-27
 */
public class AnalysisResult extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 自增主键 */
    private Long id;

    /** 关联实时数据ID */
    @Excel(name = "关联实时数据ID")
    private Long dataId;

    /** 预警类型（CPU过高/内存过高/磁盘过高） */
    @Excel(name = "预警类型", readConverterExp = "CPU过高/内存过高/磁盘过高")
    private String warningType;

    /** 预警级别（基础预警） */
    @Excel(name = "预警级别", readConverterExp = "基础预警")
    private String warningLevel;

    /** 分析时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "分析时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date analysisTime;

    /** 处理状态（0=未处理，1=已处理） */
    @Excel(name = "处理状态", readConverterExp = "0=未处理，1=已处理")
    private Integer isHandled;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setDataId(Long dataId) 
    {
        this.dataId = dataId;
    }

    public Long getDataId() 
    {
        return dataId;
    }

    public void setWarningType(String warningType) 
    {
        this.warningType = warningType;
    }

    public String getWarningType() 
    {
        return warningType;
    }

    public void setWarningLevel(String warningLevel) 
    {
        this.warningLevel = warningLevel;
    }

    public String getWarningLevel() 
    {
        return warningLevel;
    }

    public void setAnalysisTime(Date analysisTime) 
    {
        this.analysisTime = analysisTime;
    }

    public Date getAnalysisTime() 
    {
        return analysisTime;
    }

    public void setIsHandled(Integer isHandled) 
    {
        this.isHandled = isHandled;
    }

    public Integer getIsHandled() 
    {
        return isHandled;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("dataId", getDataId())
            .append("warningType", getWarningType())
            .append("warningLevel", getWarningLevel())
            .append("analysisTime", getAnalysisTime())
            .append("isHandled", getIsHandled())
            .toString();
    }
}
