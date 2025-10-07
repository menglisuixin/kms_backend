package com.ruoyi.kms.service;

/**
 * KMS数据采集服务接口
 */
public interface IKmsDataCollectionService {
    /**
     * 执行一次完整的数据采集、存储和分析流程
     */
    void executeCollection();
}