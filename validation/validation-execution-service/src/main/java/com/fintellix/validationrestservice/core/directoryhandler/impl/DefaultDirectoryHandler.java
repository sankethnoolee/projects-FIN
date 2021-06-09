/**
 * 
 */
package com.fintellix.validationrestservice.core.directoryhandler.impl;

import java.io.File;

import org.springframework.stereotype.Component;

import com.fintellix.validationrestservice.core.directoryhandler.DirectoryHandler;
import com.fintellix.validationrestservice.core.directoryhandler.DirectoryHandlerType;
import com.fintellix.validationrestservice.util.ApplicationProperties;
import com.fintellix.validationrestservice.util.ValidationStringUtils;

/**
 * @author sumeet.tripathi
 *
 */
@Component
public class DefaultDirectoryHandler implements DirectoryHandler {

	@Override
	public String getDirectoryPath(Integer runId) {
		String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim() + runId
				+ File.separator;
		outputDirectory = ValidationStringUtils.replace(outputDirectory, "\\", "/", -1, true);
		return outputDirectory;
	}

	@Override
	public String handlerType() {
		return DirectoryHandlerType.DEFAULT.getValue();
	}

}
