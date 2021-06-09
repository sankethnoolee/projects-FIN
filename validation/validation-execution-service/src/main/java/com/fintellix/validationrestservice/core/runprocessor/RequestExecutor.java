package com.fintellix.validationrestservice.core.runprocessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fintellix.framework.validation.dto.ValidationMaster;
import com.fintellix.framework.validation.dto.ValidationRequest;
import com.fintellix.validationrestservice.core.parser.ExpressionParser;

/**
 * @author sumeet.tripathi
 */
@Component
@Scope("prototype")
public class RequestExecutor implements Callable<Void> {
	private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

	@Autowired
	private Function<String, ExpressionParser> expressionParserBeanFactory;

	private List<ValidationMaster> vmList;
	private ValidationRequest request;

	@Override
	public Void call() throws Exception {
		LOGGER.info("Execution started");

		Map<Integer, String> exprMap = new HashMap<>();
		Map<Integer, ValidationMaster> vmMap = new HashMap<>();
		ExpressionParser parser = getExpressionParserInstance(System.currentTimeMillis() + "_" + Math.random()+"_"+request.getRunId());

		if (vmList != null && !vmList.isEmpty()) {
			for (ValidationMaster vm : vmList) {
				if (vm.getValidationExpression() != null && vm.getValidationExpression().trim().length() > 0) {

					exprMap.put(vm.getValidationId(), parser
							.convertIfThenElseToTernaryAndProcessPriorityBrackets(vm.getValidationExpression().trim()));
					vmMap.put(vm.getValidationId(), vm);
				}
			}
		}

		parser.init(exprMap, request, vmMap);
		LOGGER.info("Execution finished");

		return null;
	}

	public ExpressionParser getExpressionParserInstance(String name) {
		ExpressionParser bean = expressionParserBeanFactory.apply(name);
		return bean;
	}

	public void init(List<ValidationMaster> vmList, ValidationRequest request) {
		this.vmList = vmList;
		this.request = request;
	}
}
