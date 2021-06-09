package com.fintellix.validationrestservice.core.runprocessor;

import com.fintellix.framework.validation.dto.ValidationMaster;
import com.fintellix.framework.validation.dto.ValidationRequest;
import com.fintellix.validationrestservice.definition.ValidationConstants;
import com.fintellix.validationrestservice.util.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author sumeet.tripathi
 */
@Component
public class RequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

    @Autowired
    private Function<String, RequestExecutor> requestExecutorBeanFactory;

    private ExecutorService runner = null;
    private List<Future<Void>> runningJobs = null;

    public RequestHandler() {
        LOGGER.info("Initializing Request Handler");
        init();
        LOGGER.info("Request Pool Initialized");
    }

    private void init() {
        try {
            runner = new ThreadPoolExecutor(
                    Integer.parseInt(ApplicationProperties.getValue("app.validations.request.corePoolSize")),
                    Integer.parseInt(ApplicationProperties.getValue("app.validations.request.maximumPoolSize")),
                    Integer.MAX_VALUE, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(
                            Integer.parseInt(ApplicationProperties.getValue("app.validations.request.queueCapacity"))),
                    new EnqueRequest());
            runningJobs = new ArrayList<>();
            shutdownHook();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RequestExecutor getRequestExecutorInstance(String name) {
        RequestExecutor bean = requestExecutorBeanFactory.apply(name);
        return bean;
    }

    public void addRequest(List<ValidationMaster> vmList, ValidationRequest request) {
        LOGGER.info("Adding request");
        String entityType = request.getEntityType();

        if (entityType.equalsIgnoreCase(ValidationConstants.TYPE_RETURN) ||
                entityType.equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE) ||
                entityType.equalsIgnoreCase(ValidationConstants.TYPE_ENTITY)) {
            RequestExecutor executor = getRequestExecutorInstance(System.currentTimeMillis() + "_" + Math.random() + "_" + request.getRunId());
            executor.init(vmList, request);
            runningJobs.add(runner.submit(executor));
        }
    }

    public class EnqueRequest implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (!executor.isShutdown()) {
                try {
                    LOGGER.info("Adding request to queue " + r.toString());
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    protected void shutdownHook() {
        // setup JVM shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {

                LOGGER.info("Waiting for run execution to finish");
                for (Future<Void> task : runningJobs) {
                    try {
                        task.get();
                        while (!(task.isDone() || task.isCancelled())) {
                            // do nothing
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage());
                    }
                }
                LOGGER.info("run execution finished");
                try {

                    runningJobs.clear();
                    if (runner != null) {
                        runner.shutdown();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
