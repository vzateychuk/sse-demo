package ru.vez.sse;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync    // (2) an asynchronous execution enabled by the @EnableAsync annotation
@SpringBootApplication
public class ServerSideEventsDemoApplication implements AsyncConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(ServerSideEventsDemoApplication.class, args);
    }

    @Override
    public Executor getAsyncExecutor() { // (3) prepare Executor for asynchronous processing
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();// (4)
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(100);   // ThreadPoolTaskExecutor with two core threads that may be increased to up to 100 threads
        executor.setQueueCapacity(5); // (5) important to note that without a properly configured queue capacity (5), the thread pool is not able to grow. That is because the SynchronousQueue would be used instead, limiting concurrency.
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler(){
        return new SimpleAsyncUncaughtExceptionHandler(); // (6) exception handler for exceptions thrown from the asynchronous execution
    }
}
