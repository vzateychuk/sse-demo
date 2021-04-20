package ru.vez.sse;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@SpringBootApplication
public class ServerSideEventsDemoApplication implements AsyncConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(ServerSideEventsDemoApplication.class, args);
    }

    @Override
    public Executor getAsyncExecutor() { // (3)
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();// (4)
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(5); // (5)
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler(){
        return new SimpleAsyncUncaughtExceptionHandler(); // (6)
    }
}
