package com.fintellix.validationrestservice.application;

import com.fintellix.validationrestservice.core.executor.ExpressionBatchProcessor;
import com.fintellix.validationrestservice.core.executor.ExpressionExecutor;
import com.fintellix.validationrestservice.core.executor.ExpressionProcessor;
import com.fintellix.validationrestservice.core.parser.ExpressionParser;
import com.fintellix.validationrestservice.core.resultwriter.ValidationResult;
import com.fintellix.validationrestservice.core.runprocessor.RequestExecutor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.function.Function;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
@ComponentScan({"com.fintellix.validationrestservice.*"})
@ComponentScan({"com.fintellix.platformcore.*"})
@ComponentScan({"com.fintellix.framework.validation.*"})
@EnableMongoRepositories("com.fintellix.validationrestservice.core.resultwriter.dao")
@ImportResource({"classpath:platform-dataAccessContext-hibernate.xml"})
@PropertySource({"classpath:jdbc-appdb.properties"})
@ServletComponentScan("com.fintellix.platformcore.loader")
@EnableAsync
public class    Application {
    public static void main(String[] args) {
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

    @Bean
    public Function<String, RequestExecutor> requestExecutorBeanFactory() {
        return name -> requestExecutorBean(name);
    }

    @Bean
    @Scope(value = "prototype")
    public RequestExecutor requestExecutorBean(String name) {
        return new RequestExecutor();
    }

    @Bean
    public Function<String, ExpressionParser> expressionParserBeanFactory() {
        return name -> expressionParserBean(name);
    }

    @Bean
    @Scope(value = "prototype")
    public ExpressionParser expressionParserBean(String name) {
        return new ExpressionParser();
    }

    @Bean
    public Function<String, ExpressionExecutor> expressionExecutorBeanFactory() {
        return name -> expressionExecutorBean(name);
    }

    @Bean
    @Scope(value = "prototype")
    public ExpressionExecutor expressionExecutorBean(String name) {
        return new ExpressionExecutor();
    }

    @Bean
    public Function<String, ExpressionProcessor> expressionProcessorBeanFactory() {
        return name -> expressionProcessorBean(name);
    }

    @Bean
    @Scope(value = "prototype")
    public ExpressionProcessor expressionProcessorBean(String name) {
        return new ExpressionProcessor();
    }

    @Bean
    public Function<String, ExpressionBatchProcessor> expressionBatchProcessorBeanFactory() {
        return name -> expressionBatchProcessorBean(name);
    }

    @Bean
    @Scope(value = "prototype")
    public ExpressionBatchProcessor expressionBatchProcessorBean(String name) {
        return new ExpressionBatchProcessor();
    }


    @Bean
    public Function<String, ValidationResult> validationResultBeanFactory() {
        return name -> validationResultBean(name);
    }

    @Bean
    @Scope(value = "prototype")
    public ValidationResult validationResultBean(String name) {
        return new ValidationResult();
    }

}