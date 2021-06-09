package com.fintellix.validationrestservice.core.runprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fintellix.framework.validation.dto.ValidationMaster;
import com.fintellix.validationrestservice.util.ApplicationProperties;

/**
 * @author sumeet.tripathi
 */
@Component
public class CellNavigationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellNavigationHandler.class);
    private static ExecutorService runner = null;
    private static List<Future<Void>> runningJobs = null;

    public CellNavigationHandler() {
        LOGGER.info("Initializing CellNavigationHandler");
        init();
        LOGGER.info("CellNavigationHandler Pool Initialized");
    }

    private void init() {
        try {
            runner = new ThreadPoolExecutor(Integer.parseInt(ApplicationProperties.getValue("app.validations.request.corePoolSize")),
                    Integer.parseInt(ApplicationProperties.getValue("app.validations.request.maximumPoolSize")),
                    Integer.MAX_VALUE, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(Integer.parseInt(ApplicationProperties.getValue("app.validations.request.queueCapacity"))),
                    new EnqueRequest());
            runningJobs = new ArrayList<>();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addRequest(List<ValidationMaster> vmList) {
        LOGGER.info("Adding request");
        CellNavigationExecutor executor = new CellNavigationExecutor();
        executor.init(vmList);
        runningJobs.add(runner.submit(executor));
    }

    protected static void shutdownHook() {
		// setup JVM shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOGGER.info("Running shutdown hook");
				try {
					runningJobs.clear();
					runner.shutdown();
				} catch (Exception  e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		});

		LOGGER.info("Shut Down Hook Attached for runningJobs");

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

}
