package com.zxw;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(JavaSimpleJob.class);

    public static void main(String[] args) {
        ScheduleJobBootstrap bootstrap = setUpSimpleJob(setUpRegistryCenter());

        try {
            Thread.sleep(10_000);
            bootstrap.shutdown();
            log.info("中断定时任务");
        } catch (InterruptedException ignore) {
        }
    }

    private static CoordinatorRegistryCenter setUpRegistryCenter() {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("localhost:2181", "elasticjob");
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(zkConfig);
        result.init();
        return result;
    }

    private static ScheduleJobBootstrap setUpSimpleJob(final CoordinatorRegistryCenter regCenter) {
        LocalTime tenSecondsLater = LocalTime.now().plusSeconds(5);

        // 10s 后启动
        String cronExpression = String.format("%d %d %d * * ?", tenSecondsLater.getSecond(), tenSecondsLater.getMinute(), tenSecondsLater.getHour());
        ScheduleJobBootstrap bootstrap = new ScheduleJobBootstrap(
                regCenter,
                new JavaSimpleJob(),
                JobConfiguration
                        // work in the quart thread
                        //  .newBuilder("javaSimpleJob", 1)
                        // work in the ElasticJobExecutor thread
                        .newBuilder("javaSimpleJob", 2)
                        .cron(cronExpression)
                        .build()
        );
        bootstrap.schedule();

        log.info("定时任务将于 " + cronExpression + "启动");

        return bootstrap;
    }
}
