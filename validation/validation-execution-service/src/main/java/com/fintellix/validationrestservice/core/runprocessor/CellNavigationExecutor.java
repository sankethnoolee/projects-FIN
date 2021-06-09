package com.fintellix.validationrestservice.core.runprocessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fintellix.framework.validation.dto.ValidationMaster;
import com.fintellix.validationrestservice.core.cellnavigationprocessor.CellNavigationExpressionParser;

/**
 * @author sumeet.tripathi
 */
public class CellNavigationExecutor implements Callable<Void> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CellNavigationExecutor.class);

	private List<ValidationMaster> vmList;

	@Override
	public Void call() throws Exception {
		long start = System.currentTimeMillis();
		LOGGER.info("Execution started");

		if (vmList != null && !vmList.isEmpty()) {
			for (ValidationMaster vm : vmList) {

				try {
					Map<Integer, String> exprMap = new HashMap<>();
					Map<Integer, ValidationMaster> vmMap = new HashMap<>();
					CellNavigationExpressionParser parser = new CellNavigationExpressionParser();

					if (vm.getValidationExpression() != null && vm.getValidationExpression().trim().length() > 0) {

						exprMap.put(vm.getValidationId(), parser.convertIfThenElseToTernaryAndProcessPriorityBrackets(
								vm.getValidationExpression().trim()));
						vmMap.put(vm.getValidationId(), vm);
						parser.init(exprMap, vmMap);
					}
				} catch (Exception e) {
					// do-nothing
				}

			}
		}
		LOGGER.info("Execution finished in " + (System.currentTimeMillis() - start));

		return null;
	}

	void init(List<ValidationMaster> vmList) {
		this.vmList = vmList;

	}
}
