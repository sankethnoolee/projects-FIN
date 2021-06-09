package com.fintellix.collaborationservice.application;

import java.util.concurrent.Executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class,HibernateJpaAutoConfiguration.class,DataSourceTransactionManagerAutoConfiguration.class})
@EnableCaching
@ComponentScan({"com.fintellix.collaborationservice.*"})
@ComponentScan({"com.fintellix.framework.collaboration.*"})
@ImportResource({"classpath:platform-dataAccessContext-hibernate.xml","classpath:modules-collaboration-hbm.xml","classpath:common-dataAccessContext-hibernate.xml","classpath:DATAHUB-dataAccessContext-hibernate.xml"})
@PropertySource({"classpath:jdbc-appdb.properties","classpath:jdbc-marts.properties"})
@EnableAsync
public class Application 
{
    public static void main( String[] args )
    {
        SpringApplication app = new SpringApplication(Application.class);
        app.run();
        
    }
    
    @Bean
    public Executor getAsyncExecutor() {
    	ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    	executor.setCorePoolSize(50);
    	executor.setMaxPoolSize(100);
    	executor.setQueueCapacity(500);
    	executor.setThreadNamePrefix("threadPoolExecutor-");
    	executor.initialize();
    	return executor;
    }
}
