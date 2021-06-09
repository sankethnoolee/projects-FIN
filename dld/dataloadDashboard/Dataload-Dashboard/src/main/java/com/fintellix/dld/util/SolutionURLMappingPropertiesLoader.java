package com.fintellix.dld.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolutionURLMappingPropertiesLoader extends AbstractPropertyLoader{
	
	private static final String CONFIG = "solution.json";
	private final Map<String, SolutionURL> solutionsURLMAP;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SolutionURLMappingPropertiesLoader.class);

	private static final SolutionURLMappingPropertiesLoader instance = new SolutionURLMappingPropertiesLoader();

	private SolutionURLMappingPropertiesLoader() {

		solutionsURLMAP = new ConcurrentHashMap<String, SolutionURL>();
		try {

			this.configFile = CONFIG;
			load(init());
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	public class SolutionURL {
		
		private String solutionName;
		private Integer solutionId;
		private String solutionURL;
		private String clientCode;
		
		public String getSolutionName() {
			return solutionName;
		}
		public void setSolutionName(String solutionName) {
			this.solutionName = solutionName;
		}
		public Integer getSolutionId() {
			return solutionId;
		}
		public void setSolutionId(Integer solutionId) {
			this.solutionId = solutionId;
		}
		public String getSolutionURL() {
			return solutionURL;
		}
		public void setSolutionURL(String solutionURL) {
			this.solutionURL = solutionURL;
		}
		public String getClientCode() {
			return clientCode;
		}
		public void setClientCode(String clientCode) {
			this.clientCode = clientCode;
		}
		
		
	}

	@Override
	public void load(JSONObject jsonObject) throws Throwable {
		// TODO Auto-generated method stub
		if (jsonObject == null)
			return; // we don't have solution.json file
		JSONArray solutions = (JSONArray) jsonObject.get("solutions");
		JSONObject solution;
		for (Object o : solutions) {
			SolutionURL solutionURLObj = new SolutionURL();
			solution = (JSONObject) o;
			String solutionName = (String)solution.get("solution_name");
			solutionURLObj.setSolutionId(((Long)solution.get("solution_id")).intValue());
			solutionURLObj.setSolutionName(solutionName);
			solutionURLObj.setSolutionURL( (String)solution.get("solution_url"));
			solutionURLObj.setClientCode( (String)solution.get("clientCode"));
			this.solutionsURLMAP.put(solutionName, solutionURLObj);
		}
		
		
		
	}
	
	/**
	 * @return the instance
	 */
	public static SolutionURLMappingPropertiesLoader getInstance() {
		return instance;
	}
	
	public Map<String, SolutionURL> getSolutions() {
		return solutionsURLMAP;
	}
	
	
}


