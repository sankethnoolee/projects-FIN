package com.fintellix.framework.collaboration.api.controller;

import java.util.HashMap;
import java.util.Map;

import io.swagger.annotations.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fintellix.framework.collaboration.bo.DocumentManagerBo;
import com.fintellix.platformcore.services.message.RequestResponse;

@RestController
@Api(value = "Document Repository API", description = "REST API for document repository")
@RequestMapping("services/collaborationAPI")
public class CollaborationAPIController {
	@Autowired
	private DocumentManagerBo documentManagerBo;
	
	 @RequestMapping(value = "/collaborationtesturl", method = RequestMethod.GET)
	    public RequestResponse getTableMetadata(@RequestParam("name") String name) {
		 //this is test method remove this.
		 RequestResponse response = new RequestResponse();
		 Map<String,String> model = new HashMap<String,String>();
		 String test =documentManagerBo.getAllTemplateDetails().toString();
		 model.put("name", name);
		 model.put("test",test);
		 response.setModel(model);
		 return response;
	 }
}