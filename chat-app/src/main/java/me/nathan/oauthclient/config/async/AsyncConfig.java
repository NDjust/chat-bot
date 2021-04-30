package me.nathan.oauthclient.config.async;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private static final int TASK_CORE_POOL_SIZE = 2;

    private static final int TASK_MAX_POOL_SIZE = 5;

    private static final int TASK_QUEUE_CAPACITY = 0;

    private static final String EXECUTOR_BEAN_NAME = "messageExecutor";


    @Resource(name = "messageExecutor")
    private ThreadPoolTaskExecutor executor;

    @Bean(name = "messageExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(TASK_CORE_POOL_SIZE);
        executor.setMaxPoolSize(TASK_MAX_POOL_SIZE);
        executor.setQueueCapacity(TASK_QUEUE_CAPACITY);
        executor.setBeanName(EXECUTOR_BEAN_NAME);
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }

    public boolean isTaskExecutor() {
        boolean rtn = true;

        System.out.println("EXECUTOR_SAMPLE.getActiveCount() : " + executor.getActiveCount());

        // 실행중인 task 개수가 최대 개수(max + queue)보다 크거나 같으면 false
        if (executor.getActiveCount() >= (TASK_MAX_POOL_SIZE + TASK_QUEUE_CAPACITY)) {
            rtn = false;
        }

        return rtn;
    }

    public boolean isTaskExecutor(int createCount) {
        boolean rtn = true;

        System.out.println("EXECUTOR_SAMPLE.getActiveCount() : " + executor.getActiveCount());

        // 실행중인 task 개수가 최대 개수(max + queue)보다 크거나 같으면 false
        if ((executor.getActiveCount() + createCount) >= (TASK_MAX_POOL_SIZE + TASK_QUEUE_CAPACITY)) {
            rtn = false;
        }

        return rtn;
    }
}
