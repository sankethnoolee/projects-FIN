package com.fintellix.validationrestservice.core.cellnavigationprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fintellix.validationrestservice.definition.ExpressionEntityDetail;
import com.fintellix.validationrestservice.definition.RefEntityDetail;
import com.fintellix.validationrestservice.definition.ReturnEntityDetail;
import com.fintellix.validationrestservice.definition.ValidationConstants;
import com.fintellix.validationrestservice.util.ApplicationProperties;
import com.fintellix.validationrestservice.util.ObjectCloner;

public class CellNavigationMetadataBuilder {
	private List<Future<CellNavigationMetadataResolver>> runningJobs = null;
	private Map<String, Map<String, String>> mainColumnData = new ConcurrentHashMap<>();
	private Map<String, Map<String, String>> lineColumnData = new ConcurrentHashMap<>();
	private Map<String, Map<String, String>> dimensionColumnData = new ConcurrentHashMap<>();
	private Map<String, String> tableAliasData = new ConcurrentHashMap<>();
	private Map<String, Map<String, Object>> tableMetadata = new ConcurrentHashMap<>();
	private Map<String, String> tableQueryInfo = new ConcurrentHashMap<>();
	private Map<String, String> returnFormInfo = new ConcurrentHashMap<>();
	private Map<String, String> returnEntityNameInfo = new ConcurrentHashMap<>();
	private Map<String, String> returnTableLink = new ConcurrentHashMap<>();
	private Map<String, Map<String, String>> lineColumnDataType = new ConcurrentHashMap<>();
	private Map<String, Map<String, Map<String, String>>> aliaisedLineColumnData = new ConcurrentHashMap<>();

	private ExecutorService runner = null;

	public Map<String, Map<String, String>> getMainColumnData() {
		return mainColumnData;
	}

	public void setMainColumnData(Map<String, Map<String, String>> mainColumnData) {
		this.mainColumnData = mainColumnData;
	}

	public Map<String, Map<String, String>> getLineColumnData() {
		return lineColumnData;
	}

	public void setLineColumnData(Map<String, Map<String, String>> lineColumnData) {
		this.lineColumnData = lineColumnData;
	}

	public Map<String, String> getTableAliasData() {
		return tableAliasData;
	}

	public void setTableAliasData(Map<String, String> tableAliasData) {
		this.tableAliasData = tableAliasData;
	}

	public Map<String, Map<String, Object>> getTableMetadata() {
		return tableMetadata;
	}

	public void setTableMetadata(Map<String, Map<String, Object>> tableMetadata) {
		this.tableMetadata = tableMetadata;
	}

	public Map<String, Map<String, String>> getDimensionColumnData() {
		return dimensionColumnData;
	}

	public void setDimensionColumnData(Map<String, Map<String, String>> dimensionColumnData) {
		this.dimensionColumnData = dimensionColumnData;
	}

	public void createDataSet(Map<Integer, List<ExpressionEntityDetail>> expressionEntityDetails,
			Integer systemSolutionId, String systemOrgCode, Integer systemOrgId, Integer systemPeriodId,
			Integer systemRegReportVersion, Integer systemVersionNo, Integer systemRegReportId, Integer runId)
			throws Throwable {
		Map<String, List<ExpressionEntityDetail>> expressionMap = new HashMap<>();
		expressionMap.put(ValidationConstants.TYPE_RETURN, new ArrayList<>());
		expressionMap.put(ValidationConstants.TYPE_REFTABLE, new ArrayList<>());

		Map<Integer, List<ExpressionEntityDetail>> expressionEntityDetailsClone = (Map<Integer, List<ExpressionEntityDetail>>) ObjectCloner
				.deepCopy(expressionEntityDetails);

		expressionEntityDetailsClone.forEach((k, v) -> {
			v.forEach(eed -> {
				if (eed.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
					ReturnEntityDetail detail1 = (ReturnEntityDetail) eed;

					ExpressionEntityDetail entityDetail = expressionMap.get(ValidationConstants.TYPE_RETURN).stream()
							.filter(detail -> {
								if (detail.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
									ReturnEntityDetail detail2 = (ReturnEntityDetail) detail;

									return detail2.getSectionDesc().equalsIgnoreCase(detail1.getSectionDesc())
											&& detail2.getEntityCode().equalsIgnoreCase(detail1.getEntityCode());
								}
								return false;
							}).findFirst().orElse(null);

					if (entityDetail == null) {
						expressionMap.get(ValidationConstants.TYPE_RETURN).add(eed);
					} else {
						entityDetail.getEntityElements().addAll(eed.getEntityElements());

						detail1.getMetaDataInfoMap().forEach((key, val) -> {
							((ReturnEntityDetail) entityDetail).getMetaDataInfoMap().put(key, val);
						});
					}
				} else if (eed.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE)) {
					RefEntityDetail detail1 = (RefEntityDetail) eed;

					ExpressionEntityDetail entityDetail = expressionMap.get(ValidationConstants.TYPE_REFTABLE).stream()
							.filter(detail -> detail.getEntityType().equalsIgnoreCase(detail1.getEntityType())
									&& detail.getEntityCode().equalsIgnoreCase(detail1.getEntityCode()))
							.findFirst().orElse(null);

					if (entityDetail == null) {
						expressionMap.get(ValidationConstants.TYPE_REFTABLE).add(eed);
					} else {
						entityDetail.getEntityElements().addAll(eed.getEntityElements());

						detail1.getMetaDataInfoMap().forEach(
								(key, val) -> ((RefEntityDetail) entityDetail).getMetaDataInfoMap().put(key, val));
					}
				}
			});
		});

		// getting list of all the expression
		List<ExpressionEntityDetail> entityDetails = new ArrayList<>();
		expressionMap.forEach((k, v) -> entityDetails.addAll(v));

		long start = System.currentTimeMillis();

		try {
			runner = new ThreadPoolExecutor(
					Integer.parseInt(ApplicationProperties.getValue("app.validations.expression.corePoolSize")),
					Integer.parseInt(ApplicationProperties.getValue("app.validations.expression.maximumPoolSize")),
					Integer.MAX_VALUE, TimeUnit.MILLISECONDS,
					new LinkedBlockingQueue<>(Integer
							.parseInt(ApplicationProperties.getValue("app.validations.expression.queueCapacity"))),
					new EnqueRequest());

			runningJobs = new ArrayList<>();

			for (ExpressionEntityDetail eed : entityDetails) {
				if (eed.getEntityType().trim().equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
					CellNavigationMetadataResolver resolver = new CellNavigationMetadataResolver();

					resolver.init(systemPeriodId, systemSolutionId, systemVersionNo, systemRegReportVersion,
							systemOrgId, eed, systemRegReportId, runId);

					runningJobs.add(runner.submit(resolver));

				} else if (eed.getEntityType().trim().equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE)) {
					// todo

					CellNavigationMetadataResolver resolver = new CellNavigationMetadataResolver();

					resolver.init(systemPeriodId, systemSolutionId, systemVersionNo, systemRegReportVersion,
							systemOrgId, eed, systemRegReportId, runId);

					runningJobs.add(runner.submit(resolver));
				}
			}

			for(Future<CellNavigationMetadataResolver> job:runningJobs) {
            	job.get();
            }
			
			for (Future<CellNavigationMetadataResolver> task : runningJobs) {
				while (!(task.isDone() || task.isCancelled())) {
					// do nothing
				}

				try {

					if (task.get().type.equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
						// TODO code for list
						tableAliasData.put(
								task.get().type + "." + task.get().regReportName + "." + task.get().sectionDesc,
								task.get().tableName);
						returnTableLink.put(task.get().tableName,
								task.get().type + "." + task.get().regReportName + "." + task.get().sectionDesc);

						tableMetadata.computeIfAbsent(task.get().tableName, k -> new ConcurrentHashMap<>());
						tableMetadata.get(task.get().tableName).put("isGrid", task.get().isGrid);
						tableMetadata.get(task.get().tableName).put("reportName", task.get().regReportName);
						tableMetadata.get(task.get().tableName).put("sectionDesc", task.get().sectionDesc);
						tableMetadata.get(task.get().tableName).put("reportId", task.get().reportId);
						tableMetadata.get(task.get().tableName).put("sectionId", task.get().sectionId);

						tableQueryInfo.put(task.get().tableName, task.get().query);

						lineColumnDataType.put(task.get().tableName, new ConcurrentHashMap<>());
						lineColumnDataType.get(task.get().tableName).putAll(task.get().lineColumnDataType);

						aliaisedLineColumnData.put(task.get().tableName, new ConcurrentHashMap<>());
						aliaisedLineColumnData.get(task.get().tableName).putAll(task.get().aliaisedLineColumnData);
						if (mainColumnData.get(task.get().type + "." + task.get().regReportName + "."
								+ task.get().sectionDesc) == null) {
							mainColumnData.put(
									task.get().type + "." + task.get().regReportName + "." + task.get().sectionDesc,
									new ConcurrentHashMap<>());
						}
						mainColumnData
								.get(task.get().type + "." + task.get().regReportName + "." + task.get().sectionDesc)
								.putAll(task.get().allColumnData);

						if (lineColumnData.get(task.get().type + "." + task.get().regReportName + "."
								+ task.get().sectionDesc) == null) {
							lineColumnData.put(
									task.get().type + "." + task.get().regReportName + "." + task.get().sectionDesc,
									new ConcurrentHashMap<>());
						}
						lineColumnData
								.get(task.get().type + "." + task.get().regReportName + "." + task.get().sectionDesc)
								.putAll(task.get().lineColumnData);

						// dimensionColumnData
						if (dimensionColumnData.get(task.get().tableName) == null) {
							dimensionColumnData.put(task.get().tableName, new ConcurrentHashMap<>());
						}
						dimensionColumnData.get(task.get().tableName).putAll(task.get().dimensionColumnData);
						returnFormInfo.put(task.get().tableName, task.get().formName);
						if (task.get().entityName != null) {
							returnEntityNameInfo.put(task.get().tableName, task.get().entityName);
						}

					} else if (task.get().type.equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE)) {

						tableAliasData.put(task.get().type + "." + task.get().refTableName, task.get().tableName);
						returnTableLink.put(task.get().tableName, task.get().type + "." + task.get().refTableName);

						tableMetadata.computeIfAbsent(task.get().tableName, k -> new ConcurrentHashMap<>());
						// tableMetadata.get(task.get().tableName).put("isGrid", task.get().isGrid);
						// tableMetadata.get(task.get().tableName).put("reportName",
						// task.get().refTableName);
						// tableMetadata.get(task.get().tableName).put("sectionDesc",
						// task.get().sectionDesc);
						// tableMetadata.get(task.get().tableName).put("reportId", task.get().reportId);
						// tableMetadata.get(task.get().tableName).put("sectionId",
						// task.get().sectionId);

						tableQueryInfo.put(task.get().tableName, task.get().query);

						lineColumnDataType.put(task.get().tableName, new ConcurrentHashMap<>());
						lineColumnDataType.get(task.get().tableName).putAll(task.get().lineColumnDataType);

						aliaisedLineColumnData.put(task.get().tableName, new ConcurrentHashMap<>());
						aliaisedLineColumnData.get(task.get().tableName).putAll(task.get().aliaisedLineColumnData);
						if (mainColumnData.get(task.get().type + "." + task.get().refTableName + "."
								+ task.get().sectionDesc) == null) {
							mainColumnData.put(task.get().type + "." + task.get().refTableName,
									new ConcurrentHashMap<>());
						}
						mainColumnData.get(task.get().type + "." + task.get().refTableName)
								.putAll(task.get().allColumnData);

						if (lineColumnData.get(task.get().type + "." + task.get().refTableName + "."
								+ task.get().sectionDesc) == null) {
							lineColumnData.put(task.get().type + "." + task.get().refTableName,
									new ConcurrentHashMap<>());
						}
						lineColumnData.get(task.get().type + "." + task.get().refTableName)
								.putAll(task.get().lineColumnData);

						if (dimensionColumnData.get(task.get().tableName) == null) {
							dimensionColumnData.put(task.get().tableName, new ConcurrentHashMap<>());
						}
						dimensionColumnData.get(task.get().tableName).putAll(task.get().dimensionColumnData);
						returnFormInfo.put(task.get().tableName, task.get().formName);
						if (task.get().entityName != null) {
							returnEntityNameInfo.put(task.get().tableName, task.get().entityName);
						}

					} else {
						// TODO
					}
				} catch (Exception e) {
					throw e;
					// TODO: handle exception
				}
			}
			runningJobs.clear();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			shutDown();
		}
	}

	public void shutDown() throws Throwable {
		try {
			runningJobs.clear();
		} finally {
			if (runner != null) {
				runner.shutdown();
			}
		}
	}

	public Map<String, String> getTableQueryInfo() {
		return tableQueryInfo;
	}

	public void setTableQueryInfo(Map<String, String> tableQueryInfo) {
		this.tableQueryInfo = tableQueryInfo;
	}

	public Map<String, String> getReturnFormInfo() {
		return returnFormInfo;
	}

	public void setReturnFormInfo(Map<String, String> returnFormInfo) {
		this.returnFormInfo = returnFormInfo;
	}

	public Map<String, String> getReturnEntityNameInfo() {
		return returnEntityNameInfo;
	}

	public void setReturnEntityNameInfo(Map<String, String> returnEntityNameInfo) {
		this.returnEntityNameInfo = returnEntityNameInfo;
	}

	public Map<String, String> getReturnTableLink() {
		return returnTableLink;
	}

	public void setReturnTableLink(Map<String, String> returnTableLink) {
		this.returnTableLink = returnTableLink;
	}

	public Map<String, Map<String, String>> getLineColumnDataType() {
		return lineColumnDataType;
	}

	public void setLineColumnDataType(Map<String, Map<String, String>> lineColumnDataType) {
		this.lineColumnDataType = lineColumnDataType;
	}

	public Map<String, Map<String, Map<String, String>>> getAliaisedLineColumnData() {
		return aliaisedLineColumnData;
	}

	public void setAliaisedLineColumnData(Map<String, Map<String, Map<String, String>>> aliaisedLineColumnData) {
		this.aliaisedLineColumnData = aliaisedLineColumnData;
	}

	public class EnqueRequest implements RejectedExecutionHandler {

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			if (!executor.isShutdown()) {
				try {
					executor.getQueue().put(r);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}