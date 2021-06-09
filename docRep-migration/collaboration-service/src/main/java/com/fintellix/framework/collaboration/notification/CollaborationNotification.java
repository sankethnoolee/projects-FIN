package com.fintellix.framework.collaboration.notification;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fintellix.administrator.model.OrganisationUnit;
import com.fintellix.administrator.model.Users;
@Component
public interface CollaborationNotification {
	public void notifyShareDetails(Users currentUserDetails , Integer solutionId , 
			OrganisationUnit currentOrgDetails,Map<Integer, String> userIdList,
			String currentElementDisplayName, String fileType);
}
