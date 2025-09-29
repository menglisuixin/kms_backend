package com.ruoyi.kms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 关键指标实时数据对象 real_time_data
 * 
 * @author hby
 * @date 2025-09-28
 */
public class RealTimeData extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 自增主键 */
    private Long id;

    /** CPU使用率（%） */
    @Excel(name = "CPU使用率", readConverterExp = "%")
    private BigDecimal cpuUsage;

    /** 内存使用率（%） */
    @Excel(name = "内存使用率", readConverterExp = "%")
    private BigDecimal memUsage;

    /** 磁盘使用率（%） */
    @Excel(name = "磁盘使用率", readConverterExp = "%")
    private BigDecimal diskUsage;

    /** 采集时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 后端返回前端时带时分秒
    @Excel(name = "采集时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")  // 导出Excel时显示时分秒
    private Date collectTime;

    /** 数据有效性（1=有效，0=无效） */
    @Excel(name = "数据有效性", readConverterExp = "1=有效，0=无效")
    private Integer isValid;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setCpuUsage(BigDecimal cpuUsage) 
    {
        this.cpuUsage = cpuUsage;
    }

    public BigDecimal getCpuUsage() 
    {
        return cpuUsage;
    }

    public void setMemUsage(BigDecimal memUsage) 
    {
        this.memUsage = memUsage;
    }

    public BigDecimal getMemUsage() 
    {
        return memUsage;
    }

    public void setDiskUsage(BigDecimal diskUsage) 
    {
        this.diskUsage = diskUsage;
    }

    public BigDecimal getDiskUsage() 
    {
        return diskUsage;
    }

    public void setCollectTime(Date collectTime) 
    {
        this.collectTime = collectTime;
    }

    public Date getCollectTime() 
    {
        return collectTime;
    }

    public void setIsValid(Integer isValid) 
    {
        this.isValid = isValid;
    }

    public Integer getIsValid() 
    {
        return isValid;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("cpuUsage", getCpuUsage())
            .append("memUsage", getMemUsage())
            .append("diskUsage", getDiskUsage())
            .append("collectTime", getCollectTime())
            .append("isValid", getIsValid())
            .toString();
    }
}
