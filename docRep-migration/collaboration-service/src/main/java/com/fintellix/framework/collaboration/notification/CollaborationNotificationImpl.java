package com.fintellix.framework.collaboration.notification;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fintellix.administrator.model.OrganisationUnit;
import com.fintellix.administrator.model.Users;
import com.fintellix.administrator.redis.AdminCacheHelper;
import com.fintellix.platformcore.utils.EventHandler;
import com.fintellix.rest.client.dto.Event;
@Component
public class CollaborationNotificationImpl implements CollaborationNotification {
	private static Properties CollaborationProperties;
	static{
		try {
			InputStream is  = Thread.currentThread().getContextClassLoader().getResourceAsStream("collaboration.properties");
			CollaborationProperties = new Properties();
			CollaborationProperties.load(is);
		}catch (Exception e) {
			throw new RuntimeException("Coudnt read collaboration  properties from class path",e);
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CollaborationNotificationImpl.class);
	private static AdminCacheHelper adminCacheUtil = AdminCacheHelper.getInstance();
	private static final String MAIL_SUBJECT = CollaborationProperties.getProperty("app.email.subject");
	@Override
	public void notifyShareDetails(Users currentUserDetails , Integer solutionId , 
			OrganisationUnit currentOrgDetails,Map<Integer,String> csList,String currentElementDisplayName,String fileType) {
		LOGGER.info("EXEFLOW -> CollaborationNotificationImpl -> notifyShareDetails()");

		Map<String, Object> payload = null;
		String subject;
		subject = currentUserDetails.getUserName()+" ("+currentOrgDetails.getOrgName()+") "+MAIL_SUBJECT;
		for(Integer userId :csList.keySet()){
			Event e = new Event();
			EventHandler eventHandler;
			
			//setting template details and type of event which is configured
			e.setEntity("sharedNotificationMails");
			e.setCategory("Collaboration Notification");
			e.setAction("sharedNotification");

			if (solutionId != null)
				e.setSolutionId(Long.parseLong(solutionId.toString()));

			// Access role has to be displayed NA on mail for task created event
			e.addXHeader("subject", subject);
			e.addXHeader("orgId", currentOrgDetails.getOrgId());
			e.addXHeader("orgUnitName", currentOrgDetails.getOrgName());


			payload = new HashMap<String, Object>();
			payload.put("privilegeName",csList.get(userId));
			payload.put("fileName",currentElementDisplayName);
			payload.put("fileType",fileType);
			payload.put("senderInfo",currentUserDetails.getUserName()+" ("+currentOrgDetails.getOrgName()+") ");
			String message = "Now you can - ";
			switch(csList.get(userId).toUpperCase()){
			case "CREATOR":message = message+"view, modify, delete "+currentElementDisplayName;
				break;
			case "OWNER":message = message+"view, modify, delete "+currentElementDisplayName;
				break;
			case "CONSUMER":message = message+"view "+currentElementDisplayName;
				break;
			case "CONTRIBUTOR":message = message+"view, modify "+currentElementDisplayName;
				break;
			}
			
			payload.put("message",message);
			try {
				Users user = adminCacheUtil.getUserById(userId);
				if(user != null){

					e.setUserName(user.getUserName());
					e.addXHeader("userId", user.getUserId());
					e.addXHeader("to-address", user.getEmail());
					eventHandler = new EventHandler(e);
					eventHandler.addSnapshot(payload);
					eventHandler.postEvent();
				}else{
					LOGGER.info("No user found");
				}
			} catch (Throwable e1) {
				LOGGER.error("Cannot post the mail", e1);
			}
		}
	}
}
