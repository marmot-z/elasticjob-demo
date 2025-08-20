> 本项目主要用于验证 elastic job 相关问题

# 验证关闭 elastic-job 时无法中断定时任务
## 复现流程
1. 将本项目 clone 到本地，使用 Intellij IDEA 打开
2. 本地启动 zookeeper 
3. 启动 com.zxw.Main 类，等待 10 秒观察程序是否退出
4. 执行 `kill -15 ${pid}` 命令，观察程序是否退出

## 期待
当创建并启动定时任务后，调用 ScheduleJobBootstrap.shutdown 方法关闭定时任务时，对应的 jobScheduler 应该退出，用户的代码应该能正常接受线程中断，并执行相关提出逻辑后退出

## 实际
当启动下面的 `com.zxw.Main` 类后，10 秒后调用 ScheduleJobBootstrap.shutdown 方法关闭定时任务，jobScheduler 确实是执行退出逻辑，但业务代码循环没有退出。只有执行 `kill -15 ${pid}`命令时，才会触发 quartz 的 shutdownHook，从而触发用户代码的退出。并且当定时任务存在多个分片的时候，执行`kill -15 ${pid}`命令也不会导致用户代码的退出。 

## 原因 
相关代码：
```java
class JobScheduler {
    public void shutdown() {
        setUpFacade.tearDown();
        schedulerFacade.shutdownInstance();
        // 此处调用底层线程池的 shutdown 方法
        // 该方法只会中断闲置的线程，不会中断执行中的线程
        // 应该使用 ThreadPool.shutdownNow() 方法
        jobExecutor.shutdown();
        // 由于定时任务只有一个分片的时候，其在 quartz 的线程上执行
        // 所以此处应该调用 jobScheduleController.shutdown 方法执行 quartz 的退出逻辑
    }
}
```
