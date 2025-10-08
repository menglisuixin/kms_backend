package com.ruoyi.kms.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 关键指标实时数据对象 real_time_data
 *
 * @author hby
 * @date 2025-09-28
 */
@Data
public class RealTimeData extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 自增主键 */
    private Long id;

    // ====================== CPU 相关字段（保留不变）======================
    /** CPU使用率（%） */
    @Excel(name = "CPU使用率", readConverterExp = "%")
    private BigDecimal cpuUsage;

    /** CPU用户使用率（%） */
    @Excel(name = "CPU用户使用率", readConverterExp = "%")
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

    // ====================== 内存 相关字段（保留不变）======================
    /** 内存使用率（%） */
    @Excel(name = "内存使用率", readConverterExp = "%")
    private BigDecimal memUsage;

    /** 内存总量（GB） */
    @Excel(name = "内存总量", readConverterExp = "GB")
    private BigDecimal memTotal;

    /** 内存已用（GB） */
    @Excel(name = "内存已用", readConverterExp = "GB")
    private BigDecimal memUsed;

    /** 内存剩余（GB） */
    @Excel(name = "内存剩余", readConverterExp = "GB")
    private BigDecimal memFree;

    /** 磁盘数据（JSON 字符串，包含 usage、path、type、total、used、free） */
    @Excel(name = "磁盘数据", type = Excel.Type.EXPORT) // 导出时显示 JSON 字符串
    private String diskData; // 直接用 String 存储 JSON，无需自定义实体

    /** 采集时间 */
    @Excel(name = "采集时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date collectTime;

    /** 数据有效性（1=有效，0=无效） */
    @Excel(name = "数据有效性", readConverterExp = "1=有效，0=无效")
    private Integer isValid;

    public String getDiskData() {
        return diskData;
    }

    public void setDiskData(String diskData) {
        this.diskData = diskData;
    }


    // ====================== toString 方法（补充 diskData 字段）======================
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("cpuUsage", getCpuUsage())
                .append("cpuUserUsage", getCpuUserUsage())
                .append("cpuSysUsage", getCpuSysUsage())
                .append("cpuIdleUsage", getCpuIdleUsage())
                .append("cpuCoreNum", getCpuCoreNum())
                .append("memUsage", getMemUsage())
                .append("memTotal", getMemTotal())
                .append("memUsed", getMemUsed())
                .append("memFree", getMemFree())
                .append("diskData", getDiskData()) // 新增磁盘 JSON 数据
                .append("collectTime", getCollectTime())
                .append("isValid", getIsValid())
                .toString();
    }
}