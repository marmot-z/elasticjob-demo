package com.zxw;

import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class description
 *
 * @author zhangxunwei
 * @date 2025/8/20
 */
public class JavaSimpleJob implements SimpleJob {
    private static final Logger log = LoggerFactory.getLogger(JavaSimpleJob.class);

    @Override
    public void execute(final ShardingContext shardingContext) {
        log.info("Job stared");

        // 能够监听到线程中断状态，并且退出循环
        int i = 0;
        try {
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    log.warn("Thread interrupted, exit");
                    break;
                }

                log.info("i: " + i++);
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            log.warn("Thread interrupted, exit");
            Thread.currentThread().interrupt();
        }

        log.info("Job finished");
    }
}
