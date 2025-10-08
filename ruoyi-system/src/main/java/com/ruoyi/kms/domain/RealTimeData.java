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

    // ====================== 原有字段（保留不变）======================
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "采集时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date collectTime;

    /** 数据有效性（1=有效，0=无效） */
    @Excel(name = "数据有效性", readConverterExp = "1=有效，0=无效")
    private Integer isValid;

    // ====================== 新增字段（按你要求补充）======================
    /** CPU用户使用率（%） */
    @Excel(name = "CPU用户使用率", readConverterExp = "%") // 支持Excel导出
    private BigDecimal cpuUserUsage;

    /** CPU系统使用率（%） */
    @Excel(name = "CPU系统使用率", readConverterExp = "%")
    private BigDecimal cpuSysUsage;

    /** CPU空闲率（%） */
    @Excel(name = "CPU空闲率", readConverterExp = "%")
    private BigDecimal cpuIdleUsage;

    /** CPU逻辑核心数（个） */
    @Excel(name = "CPU逻辑核心数", readConverterExp = "个")
    private Integer cpuCoreNum;

    /** 内存总量（GB） */
    @Excel(name = "内存总量", readConverterExp = "GB")
    private BigDecimal memTotal;

    /** 内存已用（GB） */
    @Excel(name = "内存已用", readConverterExp = "GB")
    private BigDecimal memUsed;

    /** 内存剩余（GB） */
    @Excel(name = "内存剩余", readConverterExp = "GB")
    private BigDecimal memFree;

    /** 磁盘盘符路径（如C:/、/data） */
    @Excel(name = "磁盘路径")
    private String diskPath;

    /** 磁盘类型（如NTFS、ext4） */
    @Excel(name = "磁盘类型")
    private String diskType;

    /** 磁盘总大小（GB） */
    @Excel(name = "磁盘总大小", readConverterExp = "GB")
    private BigDecimal diskTotal;

    /** 磁盘已用大小（GB） */
    @Excel(name = "磁盘已用大小", readConverterExp = "GB")
    private BigDecimal diskUsed;

    /** 磁盘剩余大小（GB） */
    @Excel(name = "磁盘剩余大小", readConverterExp = "GB")
    private BigDecimal diskFree;

    // ====================== Getter 和 Setter（原有+新增字段都补全）======================
    // 原有字段的Getter/Setter（保留不变）
    public void setId(Long id) { this.id = id; }
    public Long getId() { return id; }
    public void setCpuUsage(BigDecimal cpuUsage) { this.cpuUsage = cpuUsage; }
    public BigDecimal getCpuUsage() { return cpuUsage; }
    public void setMemUsage(BigDecimal memUsage) { this.memUsage = memUsage; }
    public BigDecimal getMemUsage() { return memUsage; }
    public void setDiskUsage(BigDecimal diskUsage) { this.diskUsage = diskUsage; }
    public BigDecimal getDiskUsage() { return diskUsage; }
    public void setCollectTime(Date collectTime) { this.collectTime = collectTime; }
    public Date getCollectTime() { return collectTime; }
    public void setIsValid(Integer isValid) { this.isValid = isValid; }
    public Integer getIsValid() { return isValid; }

    // 新增字段的Getter/Setter
    public BigDecimal getCpuUserUsage() { return cpuUserUsage; }
    public void setCpuUserUsage(BigDecimal cpuUserUsage) { this.cpuUserUsage = cpuUserUsage; }
    public BigDecimal getCpuSysUsage() { return cpuSysUsage; }
    public void setCpuSysUsage(BigDecimal cpuSysUsage) { this.cpuSysUsage = cpuSysUsage; }
    public BigDecimal getCpuIdleUsage() { return cpuIdleUsage; }
    public void setCpuIdleUsage(BigDecimal cpuIdleUsage) { this.cpuIdleUsage = cpuIdleUsage; }
    public Integer getCpuCoreNum() { return cpuCoreNum; }
    public void setCpuCoreNum(Integer cpuCoreNum) { this.cpuCoreNum = cpuCoreNum; }
    public BigDecimal getMemTotal() { return memTotal; }
    public void setMemTotal(BigDecimal memTotal) { this.memTotal = memTotal; }
    public BigDecimal getMemUsed() { return memUsed; }
    public void setMemUsed(BigDecimal memUsed) { this.memUsed = memUsed; }
    public BigDecimal getMemFree() { return memFree; }
    public void setMemFree(BigDecimal memFree) { this.memFree = memFree; }
    public String getDiskPath() { return diskPath; }
    public void setDiskPath(String diskPath) { this.diskPath = diskPath; }
    public String getDiskType() { return diskType; }
    public void setDiskType(String diskType) { this.diskType = diskType; }
    public BigDecimal getDiskTotal() { return diskTotal; }
    public void setDiskTotal(BigDecimal diskTotal) { this.diskTotal = diskTotal; }
    public BigDecimal getDiskUsed() { return diskUsed; }
    public void setDiskUsed(BigDecimal diskUsed) { this.diskUsed = diskUsed; }
    public BigDecimal getDiskFree() { return diskFree; }
    public void setDiskFree(BigDecimal diskFree) { this.diskFree = diskFree; }

    // toString 方法（补充新增字段）
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("cpuUsage", getCpuUsage())
                .append("memUsage", getMemUsage())
                .append("diskUsage", getDiskUsage())
                .append("collectTime", getCollectTime())
                .append("isValid", getIsValid())
                .append("cpuUserUsage", getCpuUserUsage())
                .append("cpuSysUsage", getCpuSysUsage())
                .append("cpuIdleUsage", getCpuIdleUsage())
                .append("cpuCoreNum", getCpuCoreNum())
                .append("memTotal", getMemTotal())
                .append("memUsed", getMemUsed())
                .append("memFree", getMemFree())
                .append("diskPath", getDiskPath())
                .append("diskType", getDiskType())
                .append("diskTotal", getDiskTotal())
                .append("diskUsed", getDiskUsed())
                .append("diskFree", getDiskFree())
                .toString();
    }
}