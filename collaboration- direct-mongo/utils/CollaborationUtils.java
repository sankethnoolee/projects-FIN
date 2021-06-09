package com.fintellix.framework.collaboration.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;










import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fintellix.administrator.model.AccessRole;
import com.fintellix.administrator.model.OrganisationUnit;
import com.fintellix.administrator.model.Users;
import com.fintellix.administrator.redis.AdminCacheHelper;
import com.fintellix.framework.collaboration.dto.ContentSecurity;
import com.fintellix.framework.collaboration.dto.ContentSecurityAccessRole;
import com.fintellix.framework.collaboration.dto.ContentSecurityDetails;
import com.fintellix.framework.collaboration.dto.ContentSecurityDetailsAccessRole;
import com.fintellix.framework.collaboration.dto.DocumentWrapper;
import com.fintellix.platformcore.common.exception.VyasaBusinessException;
import com.fintellix.platformcore.utils.CollaborationProperties;

public class CollaborationUtils {
	private static AdminCacheHelper adminCacheUtil = AdminCacheHelper.getInstance();
	private static String SINGLE_SPACE =" "; 
	private static final String TYPE_FILE = CollaborationProperties.getValue("app.typeFileName");
	private static final String TYPE_DIRECTORY = CollaborationProperties.getValue("app.typeDirectoryName");
	public static Map<String,Integer> priorityMap = new HashMap<String,Integer>();
	static{
		// to find the top prior override the duplicate objects
		
		priorityMap.put("DENIED", 7);
		priorityMap.put("CREATOR", 6);
		priorityMap.put("OWNER", 5);
		priorityMap.put("CONTRIBUTOR", 4);
		priorityMap.put("CONSUMER", 3);
	}
	
	//method to convert result set from query to list of output Dto
	public static List<DocumentWrapper> convertResultSetToDocumentWrapper(List<Object[]> resultList) throws VyasaBusinessException, Throwable{
		Map<String,DocumentWrapper> distinctObjects = new HashMap<String, DocumentWrapper>();
		DocumentWrapper dw = new DocumentWrapper();
		/*
		 * columns order required
		 * 
		 * column 0 - ENTITY_ID,
		 * column 1 - ENTITY_NAME,
		 * column 2 - TYPE,
		 * column 3 - PRIVILEGE_NAME,
		 * column 4 - CREATED_BY,
		 * column 5 - LAST_MODIFIED_BY,
		 * column 6 - LAST_MODIFIED_TIME,
		 * column 7 - PACKAGE_PATH
		 * column 8 - CRESATED time
		 * column 9 - version_number
		 * column 10 - result_Origin
		 * column 11 - IS_PRIVATE
		 * */
		for(Object[] obj:resultList){
			dw = new DocumentWrapper();
			Users createdUserDetails = adminCacheUtil.getUserById(Integer.parseInt(obj[4].toString()));
			Users modifiedUserDetails = obj[5]==null?null:adminCacheUtil.getUserById(Integer.parseInt(obj[5].toString()));
			dw.setEntityId(obj[0].toString());
			dw.setEntityName(obj[1].toString());
			dw.setEntityType(obj[2].toString());
			dw.setPrivilegeName(obj[3].toString());
			dw.setCreatedTime(obj[8]==null?0:Long.parseLong(obj[8].toString()));
			dw.setIsPrivate((obj.length<12||obj[11]==null )?0:Integer.parseInt(obj[11].toString()));
			dw.setCreatedBy(createdUserDetails.getFirstName()+SINGLE_SPACE+createdUserDetails.getMiddleName()+SINGLE_SPACE+createdUserDetails.getLastName());
			if(modifiedUserDetails==null){
				dw.setLastModifiedBy("");
			} else {
				dw.setLastModifiedBy(modifiedUserDetails.getFirstName()+SINGLE_SPACE+modifiedUserDetails.getMiddleName()+SINGLE_SPACE+modifiedUserDetails.getLastName());
			}
			
			dw.setLastModified(obj[6]==null?0:Long.parseLong(obj[6].toString()));
			dw.setEntityPath(obj[7].toString());
			
			//doing this in order to group folders and file on sort.
			dw.setSortName(obj[2].toString().equalsIgnoreCase(TYPE_FILE)?"Z_"+obj[1].toString():"A_"+obj[1].toString());
			dw.setVersionNumber(obj[9].toString());
			dw.setResultOrigin(obj[10].toString());
			
			//for filter duplicates based on priorities(do-distinctObjects).
			String doMapKey = dw.getEntityId()+"###"+dw.getEntityType();
			if(distinctObjects.get(doMapKey)==null){
				distinctObjects.put(doMapKey,dw);
			}else{
				DocumentWrapper existingDW = distinctObjects.get(doMapKey);
				if("PRIVILEGE".equalsIgnoreCase(dw.getResultOrigin()) && "CREATOR".equalsIgnoreCase(dw.getPrivilegeName())){
					//override any existing obj this is top priority because of overriding existing one. 
					distinctObjects.put(doMapKey,dw);
				
				}else if("PRIVILEGE".equalsIgnoreCase(dw.getResultOrigin()) && !"CREATOR".equalsIgnoreCase(existingDW.getPrivilegeName())){
					//override any existing obj this is top priority because of overriding existing one. 
					//distinctObjects.put(doMapKey,dw);
					if((priorityMap.get(existingDW.getPrivilegeName())<priorityMap.get(dw.getPrivilegeName()))){
						//adding only if priority is more than existing 
						distinctObjects.put(doMapKey,dw);
					}
				}else if("PRIVILEGE_ACCESS_ROLE".equalsIgnoreCase(dw.getResultOrigin()) && !"PRIVILEGE".equalsIgnoreCase(existingDW.getResultOrigin())){
					//do nothing this is top priority. 
					//distinctObjects.put(doMapKey,dw);
					if((priorityMap.get(existingDW.getPrivilegeName())<priorityMap.get(dw.getPrivilegeName()))){
						//adding only if priority is more than existing 
						distinctObjects.put(doMapKey,dw);
					}
				}else if("CREATOR".equalsIgnoreCase(dw.getResultOrigin()) && !"PRIVILEGE".equalsIgnoreCase(existingDW.getResultOrigin())&& !"PRIVILEGE_ACCESS_ROLE".equalsIgnoreCase(existingDW.getResultOrigin())){
					//do nothing this is top priority. 
					distinctObjects.put(doMapKey,dw);
				}else if(!"PRIVILEGE".equalsIgnoreCase(existingDW.getResultOrigin()) && !"PRIVILEGE_ACCESS_ROLE".equalsIgnoreCase(existingDW.getResultOrigin())){
					if((priorityMap.get(existingDW.getPrivilegeName())<priorityMap.get(dw.getPrivilegeName()))&&!"PRIVILEGE".equalsIgnoreCase(existingDW.getResultOrigin())&& !"PRIVILEGE_ACCESS_ROLE".equalsIgnoreCase(existingDW.getResultOrigin())){
						//adding only if priority is more than existing 
						distinctObjects.put(doMapKey,dw);
					}
				}
			}
			
			//listOfDocumentWrapper.add(dw);
		}
		
		
		return distinctObjects.values().stream().collect(Collectors.toList());
	}
	//method to convert result set from query to list of output Dto setting parent privilege as default if no privilege specified
		public static List<DocumentWrapper> convertResultSetToDocumentWrapperWithParentDefaultPrivilege(List<Object[]> resultList,String parentPrivilege) throws VyasaBusinessException, Throwable{
			List<DocumentWrapper> listOfDocumentWrapper = new ArrayList<DocumentWrapper>();
			DocumentWrapper dw = new DocumentWrapper();
			Map<String,DocumentWrapper> distinctObjects = new HashMap<String, DocumentWrapper>();
			
			/*
			 * columns order required
			 * 
			 * column 0 - ENTITY_ID,
			 * column 1 - ENTITY_NAME,
			 * column 2 - TYPE,
			 * column 3 - CREATED_BY,
			 * column 4 - LAST_MODIFIED_BY,
			 * column 5 - LAST_MODIFIED_TIME,
			 * column 6 - PACKAGE_PATH,
			 * column 7 - CREATED_TIME
			 * column 8 - version number
			 * column 9 - privileges
			 * column 10 - result_Origin
			 * */
			for(Object[] obj:resultList){
				dw = new DocumentWrapper();
				Users createdUserDetails = adminCacheUtil.getUserById(Integer.parseInt(obj[3].toString()));
				Users modifiedUserDetails = obj[4]==null?null:adminCacheUtil.getUserById(Integer.parseInt(obj[4].toString()));
				dw.setEntityId(obj[0].toString());
				dw.setEntityName(obj[1].toString());
				dw.setEntityType(obj[2].toString());
				dw.setPrivilegeName(obj[9]==null?parentPrivilege:obj[9].toString());
				dw.setCreatedTime(obj[7]==null?0:Long.parseLong(obj[7].toString()));
				dw.setIsPrivate((obj.length<12||obj[11]==null )?0:Integer.parseInt(obj[11].toString()));
				dw.setCreatedBy(createdUserDetails.getFirstName()+SINGLE_SPACE+createdUserDetails.getMiddleName()+SINGLE_SPACE+createdUserDetails.getLastName());
				if(modifiedUserDetails==null){
					dw.setLastModifiedBy("");
				} else {
					dw.setLastModifiedBy(modifiedUserDetails.getFirstName()+SINGLE_SPACE+modifiedUserDetails.getMiddleName()+SINGLE_SPACE+modifiedUserDetails.getLastName());
				}
				
				dw.setLastModified(obj[5]==null?0:Long.parseLong(obj[5].toString()));
				
				dw.setEntityPath(obj[6].toString());
				
				//doing this in order to group folders and file on sort.
				dw.setSortName(obj[2].toString().equalsIgnoreCase(TYPE_FILE)?"Z_"+obj[1].toString():"A_"+obj[1].toString());
				dw.setVersionNumber(obj[8].toString());
				dw.setResultOrigin(obj[10].toString());
				
				//for filter duplicates based on priorities.
				String doMapKey = dw.getEntityId()+"###"+dw.getEntityType();
				if(distinctObjects.get(doMapKey)==null){
					distinctObjects.put(doMapKey,dw);
				}else{
					DocumentWrapper existingDW = distinctObjects.get(doMapKey);
					if("PRIVILEGE".equalsIgnoreCase(dw.getResultOrigin()) && "CREATOR".equalsIgnoreCase(dw.getPrivilegeName())){
						//override any existing obj this is top priority because of overriding existing one. 
						distinctObjects.put(doMapKey,dw);
					
					}else if("PRIVILEGE".equalsIgnoreCase(dw.getResultOrigin()) && !"CREATOR".equalsIgnoreCase(existingDW.getPrivilegeName())){
						//override any existing obj this is top priority because of overriding existing one. 
						//distinctObjects.put(doMapKey,dw);
						if((priorityMap.get(existingDW.getPrivilegeName())<priorityMap.get(dw.getPrivilegeName()))){
							//adding only if priority is more than existing 
							distinctObjects.put(doMapKey,dw);
						}
					}else if("PRIVILEGE_ACCESS_ROLE".equalsIgnoreCase(dw.getResultOrigin()) && !"PRIVILEGE".equalsIgnoreCase(existingDW.getResultOrigin())){
						//do nothing this is top priority. 
						//distinctObjects.put(doMapKey,dw);
						if((priorityMap.get(existingDW.getPrivilegeName())<priorityMap.get(dw.getPrivilegeName()))){
							//adding only if priority is more than existing 
							distinctObjects.put(doMapKey,dw);
						}
					}else if("CREATOR".equalsIgnoreCase(dw.getResultOrigin()) && !"PRIVILEGE".equalsIgnoreCase(existingDW.getResultOrigin())&& !"PRIVILEGE_ACCESS_ROLE".equalsIgnoreCase(existingDW.getResultOrigin())){
						//do nothing this is top priority. 
						distinctObjects.put(doMapKey,dw);
					}else if(!"PRIVILEGE".equalsIgnoreCase(existingDW.getResultOrigin()) && !"PRIVILEGE_ACCESS_ROLE".equalsIgnoreCase(existingDW.getResultOrigin())){
						if((priorityMap.get(existingDW.getPrivilegeName())<priorityMap.get(dw.getPrivilegeName()))&&!"PRIVILEGE".equalsIgnoreCase(existingDW.getResultOrigin())&& !"PRIVILEGE_ACCESS_ROLE".equalsIgnoreCase(existingDW.getResultOrigin())){
							//adding only if priority is more than existing 
							distinctObjects.put(doMapKey,dw);
						}
					}
				}
				//listOfDocumentWrapper.add(dw);
			}
			
			
			return distinctObjects.values().stream().collect(Collectors.toList());
		}
		public static List<ContentSecurityDetails> convertContentSecurityListToDetailList(List<ContentSecurity> csList) throws VyasaBusinessException, Throwable{
			List<ContentSecurityDetails> csdList = new ArrayList<ContentSecurityDetails>();
			ContentSecurityDetails csd = null;
			for(ContentSecurity cs:csList){
				Users userDetails = adminCacheUtil.getUserById(cs.getUserId());
				OrganisationUnit ou=adminCacheUtil.getOrganisationById(cs.getOrgId());
				csd=new ContentSecurityDetails();
				csd.setContentId(cs.getContentId());
				csd.setContentSecurityId(cs.getContentSecurityId());
				csd.setContentTypeId(cs.getContentTypeId());
				csd.setOrgId(cs.getOrgId());
				csd.setSecurityTemplateName(cs.getSecurityTemplateName());
				csd.setSolutionId(cs.getSolutionId());
				csd.setUserId(cs.getUserId());
				csd.setUserName(userDetails.getFirstName()+SINGLE_SPACE+userDetails.getMiddleName()+SINGLE_SPACE+userDetails.getLastName());
				csd.setOrgName(ou.getOrgName());
				csdList.add(csd);
			}
			return csdList;
		}
		
		public static Users getUserDetailById(Integer userId) throws Throwable{
			return adminCacheUtil.getUserById(userId);
		}
		public static List<ContentSecurityDetailsAccessRole> convertContentSecurityAccessRoleListToDetailList(List<ContentSecurityAccessRole> csList) throws VyasaBusinessException, Throwable{
			List<ContentSecurityDetailsAccessRole> csdList = new ArrayList<ContentSecurityDetailsAccessRole>();
			ContentSecurityDetailsAccessRole csd = null;
			List<AccessRole> roleDetails = adminCacheUtil.getActiveAccessRoles();
			for(ContentSecurityAccessRole cs:csList){
				OrganisationUnit ou=adminCacheUtil.getOrganisationById(cs.getOrgId());
				csd=new ContentSecurityDetailsAccessRole();
				csd.setContentId(cs.getContentId());
				csd.setContentSecurityId(cs.getContentSecurityId());
				csd.setContentTypeId(cs.getContentTypeId());
				csd.setOrgId(cs.getOrgId());
				csd.setSecurityTemplateName(cs.getSecurityTemplateName());
				csd.setSolutionId(cs.getSolutionId());
				csd.setRoleId(cs.getRoleId());
				csd.setRoleName(roleDetails.stream().filter(ar->ar.getAccessRoleId().equals(cs.getRoleId())).collect(Collectors.toList()).get(0).getAccessRoleName());
				csd.setOrgName(ou.getOrgName());
				csdList.add(csd);
			}
			return csdList;
		}
		
		//method to convert result set from query to list of output Dto 
		//** keeping this method separate in order to handle properties or old methods can be re used
		public static List<DocumentWrapper> convertResultSetToDocumentWrapperForSearch(List<Object[]> resultList) throws VyasaBusinessException, Throwable{
			List<DocumentWrapper> listOfDocumentWrapper = new ArrayList<DocumentWrapper>();
			DocumentWrapper dw = new DocumentWrapper();
			Map<String,DocumentWrapper> distinctObjects = new HashMap<String, DocumentWrapper>();
			
			/*
			 * columns order required
			 * 
			 * column 0 - ENTITY_ID,
			 * column 1 - ENTITY_NAME,
			 * column 2 - TYPE,
			 * column 3 - CREATED_BY,
			 * column 4 - LAST_MODIFIED_BY,
			 * column 5 - LAST_MODIFIED_TIME,
			 * column 6 - PACKAGE_PATH,
			 * column 7 - CREATED_TIME
			 * column 8 - version number
			 * column 9 - privileges
			 * column 10 - result_Origin
			 * */
			for(Object[] obj:resultList){
				dw = new DocumentWrapper();
				Users createdUserDetails = adminCacheUtil.getUserById(Integer.parseInt(obj[3].toString()));
				Users modifiedUserDetails = obj[4]==null?null:adminCacheUtil.getUserById(Integer.parseInt(obj[4].toString()));
				dw.setEntityId(obj[0].toString());
				dw.setEntityName(obj[1].toString());
				dw.setEntityType(obj[2].toString());
				dw.setPrivilegeName(obj[9]==null?null:obj[9].toString());
				dw.setCreatedTime(obj[7]==null?0:Long.parseLong(obj[7].toString()));
				dw.setIsPrivate((obj.length<12||obj[11]==null )?0:Integer.parseInt(obj[11].toString()));
				dw.setCreatedBy(createdUserDetails.getFirstName()+SINGLE_SPACE+createdUserDetails.getMiddleName()+SINGLE_SPACE+createdUserDetails.getLastName());
				if(modifiedUserDetails==null){
					dw.setLastModifiedBy("");
				} else {
					dw.setLastModifiedBy(modifiedUserDetails.getFirstName()+SINGLE_SPACE+modifiedUserDetails.getMiddleName()+SINGLE_SPACE+modifiedUserDetails.getLastName());
				}
				
				dw.setLastModified(obj[5]==null?0:Long.parseLong(obj[5].toString()));
				
				dw.setEntityPath(obj[6].toString());
				
				//doing this in order to group folders and file on sort.
				dw.setSortName(obj[2].toString().equalsIgnoreCase(TYPE_FILE)?"Z_"+obj[1].toString():"A_"+obj[1].toString());
				dw.setVersionNumber(obj[8].toString());
				dw.setResultOrigin(obj[10].toString());
				
				//for filter duplicates based on priorities.
				String doMapKey = dw.getEntityId()+"###"+dw.getEntityType();
				if(distinctObjects.get(doMapKey)==null){
					distinctObjects.put(doMapKey,dw);
				}else{
					DocumentWrapper existingDW = distinctObjects.get(doMapKey);
					if("PRIVILEGE".equalsIgnoreCase(dw.getResultOrigin()) && "CREATOR".equalsIgnoreCase(dw.getPrivilegeName())){
						//override any existing obj this is top priority because of overriding existing one. 
						distinctObjects.put(doMapKey,dw);
					
					}else if("PRIVILEGE".equalsIgnoreCase(dw.getResultOrigin()) && !"CREATOR".equalsIgnoreCase(existingDW.getPrivilegeName())){
						//override any existing obj this is top priority because of overriding existing one. 
						//distinctObjects.put(doMapKey,dw);
						if((existingDW.getPrivilegeName()==null)||(priorityMap.get(existingDW.getPrivilegeName())<priorityMap.get(dw.getPrivilegeName()))){
							//adding only if priority is more than existing 
							distinctObjects.put(doMapKey,dw);
						}
					}else if("PRIVILEGE_ACCESS_ROLE".equalsIgnoreCase(dw.getResultOrigin()) && !"PRIVILEGE".equalsIgnoreCase(existingDW.getResultOrigin())){
						//do nothing this is top priority. 
						//distinctObjects.put(doMapKey,dw);
						if((existingDW.getPrivilegeName()==null)||(priorityMap.get(existingDW.getPrivilegeName())<priorityMap.get(dw.getPrivilegeName()))){
							//adding only if priority is more than existing 
							distinctObjects.put(doMapKey,dw);
						}
					}else if("CREATOR".equalsIgnoreCase(dw.getResultOrigin()) && !"PRIVILEGE".equalsIgnoreCase(existingDW.getResultOrigin())&& !"PRIVILEGE_ACCESS_ROLE".equalsIgnoreCase(existingDW.getResultOrigin())){
						//do nothing this is top priority. 
						distinctObjects.put(doMapKey,dw);
					}else if(!"PRIVILEGE".equalsIgnoreCase(existingDW.getResultOrigin()) && !"PRIVILEGE_ACCESS_ROLE".equalsIgnoreCase(existingDW.getResultOrigin())){
						if((existingDW.getPrivilegeName()==null)||(priorityMap.get(existingDW.getPrivilegeName())<priorityMap.get(dw.getPrivilegeName()))&&!"PRIVILEGE".equalsIgnoreCase(existingDW.getResultOrigin())&& !"PRIVILEGE_ACCESS_ROLE".equalsIgnoreCase(existingDW.getResultOrigin())){
							//adding only if priority is more than existing 
							distinctObjects.put(doMapKey,dw);
						}
					}
				}
				//listOfDocumentWrapper.add(dw);
			}
			
			
			return distinctObjects.values().stream().collect(Collectors.toList());
		}
		

}
