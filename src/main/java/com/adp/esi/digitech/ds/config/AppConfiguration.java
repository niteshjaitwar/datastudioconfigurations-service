package com.adp.esi.digitech.ds.config;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@Configuration
@EnableTransactionManagement
@EnableAsync
@EnableEncryptableProperties
public class AppConfiguration {
	
	@Value("${app.daemon-tasks.thread-core-pool-size:20}")
	private int daemonTasksThreadCorePoolSize;
	
	@Value("${app.daemon-tasks.thread-max-pool-size:50}")
	private int daemonTasksThreadMaxPoolSize;
	
	@Value("${app.daemon-tasks.thread-queue-capacity:5000}")
	private int daemonTasksThreadQueueCapacity;
	
	@Bean
	public Executor asyncExecutor () {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(daemonTasksThreadCorePoolSize);
		executor.setMaxPoolSize(daemonTasksThreadMaxPoolSize);
	    executor.setQueueCapacity(daemonTasksThreadQueueCapacity);
	    executor.setThreadNamePrefix("DSThread-");
	    executor.initialize();
		return executor;
	}
	
	@Bean
    public CacheManager cacheManagers() {
        return new ConcurrentMapCacheManager("TransformationRulesCache", "ValidationRulesCache");
    }

}
