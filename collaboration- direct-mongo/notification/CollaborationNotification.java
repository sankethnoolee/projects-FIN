package com.fintellix.framework.collaboration.notification;

import java.util.List;
import java.util.Map;

import com.fintellix.administrator.model.OrganisationUnit;
import com.fintellix.administrator.model.Users;
import com.fintellix.framework.collaboration.dto.ContentSecurity;
import com.fintellix.framework.collaboration.dto.ContentSecurityAccessRole;

public interface CollaborationNotification {
	public void notifyShareDetails(Users currentUserDetails , Integer solutionId , 
			OrganisationUnit currentOrgDetails,Map<Integer, String> userIdList,
			String currentElementDisplayName, String fileType);
}
