package com.ruoyi.quartz.job;

import com.ruoyi.quartz.domain.SysJob;
import com.ruoyi.quartz.util.JobInvokeUtil;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * KMS系统数据采集定时任务
 * 这是一个标准的、与若依框架完全集成的Quartz Job。
 */
@DisallowConcurrentExecution
public class DataCollectorJob extends QuartzJobBean {

    private static final Logger log = LoggerFactory.getLogger(DataCollectorJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // 直接使用硬编码的字符串 "JOB_PARAM_KEY" 来获取SysJob对象
        // 这可以避免因版本不同导致的常量名不一致问题
        SysJob sysJob = (SysJob) context.getMergedJobDataMap().get("JOB_PARAM_KEY");

        try {
            log.info("--- 调度器触发KMS数据采集任务，任务ID: {} ---", sysJob.getJobId());

            JobInvokeUtil.invokeMethod(sysJob);

            log.info("--- KMS数据采集任务调度成功 ---");
        } catch (Exception e) {
            log.error("KMS数据采集任务调度失败！", e);
            throw new JobExecutionException(e);
        }
    }
}