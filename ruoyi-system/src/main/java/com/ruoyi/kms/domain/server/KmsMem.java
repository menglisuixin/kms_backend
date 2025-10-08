package com.ruoyi.kms.domain.server;

import com.ruoyi.common.utils.Arith;

/**
 * 內存相关信息
 * 
 * @author ruoyi
 */
public class KmsMem
{
    /** 内存总量（字节） */
    private long total;
    /** 内存已用（字节） */
    private long used;
    /** 内存可用（字节） */
    private long free;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public long getFree() {
        return free;
    }

    public void setFree(long free) {
        this.free = free;
    }
}
