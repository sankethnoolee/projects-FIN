package com.fintellix.validationrestservice.definition;

import com.fintellix.framework.validation.dto.ValidationRequest;

public interface ValidationResultStatusManager {
	
	String getSupportedEntityType();
	void addValidationResultStatus(ValidationRequest validationRequest)  throws Exception;
}
