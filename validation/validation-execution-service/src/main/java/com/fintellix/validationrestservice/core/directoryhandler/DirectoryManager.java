/**
 * 
 */
package com.fintellix.validationrestservice.core.directoryhandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fintellix.validationrestservice.util.ApplicationProperties;

/**
 * @author sumeet.tripathi
 *
 */
@Component
public class DirectoryManager {

	private Map<String, DirectoryHandler> directoryHandlers = new HashMap<>();

	@Autowired
	public DirectoryManager(List<DirectoryHandler> providers) {
		for (DirectoryHandler writer : providers) {
			directoryHandlers.put(writer.handlerType(), writer);
		}
	}

	public String getDirectoryPath(Integer runId) {
		return directoryHandlers.get(ApplicationProperties.getValue("app.validations.directoryhandler")).getDirectoryPath(runId);

	}

}
