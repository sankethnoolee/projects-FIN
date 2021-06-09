/*********************************************************************************
 * TODO DESCRIPTION: 
 * Created on Aug 24, 2006
 * Copyright (C) 2006 i-Create Software India Pvt Ltd. All Rights Reserved.
 ********************************************************************************/
/**
 * 
 */
package com.fintellix.platformcore.common.exception;

/**
 * @author angshu
 *
 */
public class ErrorCodes {
	
	private ErrorCodes() {
		//DO NOTHING
	}
  
   public static final int UNEXPECTED_ERROR = 50;
   public static final int DATA_ACCESS_ERROR = 51;
   
   public static final int INVALID_LOGIN    = 100;
   public static final int INVALID_BIZ_UNIT = 101;
   public static final int INVALID_ADDRESS  = 102;
   public static final int INVALID_USERNAME = 103;
   public static final int INVALID_LOCATION = 104;
   public static final int INVALID_USER_GROUP = 105;
   public static final int USER_ALREADY_EXISTS = 106;
   public static final int NO_ACCESS_PRIVILEGE = 107;
   public static final int SEARCH_TYPE_UNDEFINED = 108;
   public static final int SEARCH_TYPE_NOT_SPECIFIED = 109;
   
   /*
    * For analysis errors
    */
   public static final int DASHBOARD_NOT_SAVED = 501;
   public static final int ANALYSIS_NOT_SAVED = 502;
   public static final int IN_SESSION_ANALYSIS_CREATION_NOT_FOUND = 503;
   public static final int ANALYSIS_NAME_EMPTY = 504;
   public static final int ANALYSIS_DESC_EMPTY = 505;
   public static final int ANALYSIS_TYPE_EMPTY = 506;
   public static final int ANALYSIS_LOAD_ERROR = 507;
   
   public static final int GENERIC_OLAP_ERROR = 550;
   
   /*
    * For report errors
    */
   public static final int REPORT_NOT_FOUND    = 1001;
   public static final int REPORT_RENDER_ERROR = 1002;
   public static final int REPORT_NOT_SAVED    = 1003;
   public static final int REPORT_NAME_EMPTY   = 1004;
   public static final int REPORT_DESC_EMPTY   = 1005;
   public static final int REPORT_TYPE_EMPTY   = 1006;
   public static final int REPORT_DEPT_EMPTY   = 1007;
   
   
   /*
    * For user errors
    */
   public static final int USER_NAME_EMPTY = 1501;
   public static final int USER_PWD_EMPTY = 1502;
   public static final int USER_FIRSTNAME_EMPTY = 1503;
   public static final int USER_LASTNAME_EMPTY = 1504;
   public static final int USER_DOORNO_EMPTY = 1505;
   public static final int USER_CITY_EMPTY = 1506;
   public static final int USER_STATE_EMPTY = 1507;
   public static final int USER_COUNTRY_EMPTY = 1508;
   public static final int USER_ZIPCODE_EMPTY = 1509;
   public static final int USER_GENDER_EMPTY = 1510;
   public static final int USER_PHONENO_EMPTY = 1511;
   public static final int USER_EMAIL_ADDR_EMPTY = 1512;
   public static final int USER_NOT_SAVED = 1513;
   public static final int USER_NOT_FOUND = 1514;
   public static final int DUPLCATE_EMP_ID = 1515;

   /*
    * For bizscore solution errors
    */
   public static final int NO_DATA_TO_BUILD_DATASET = 1600;
   public static final int NO_TASK_DEF = 1601;
   public static final int UNABLE_TO_READ_REQUEST_OBJ = 1602;
   public static final int ERROR_EXECUTING_TASK = 1603;
   public static final int NO_TASK_ACTION = 1604;

   public static final int GROUP_NAME_EMPTY = 1700;
   public static final int GROUP_DESC_EMPTY = 1701;
   public static final int GROUP_ORG_HIERARCHY_EMPTY = 1702;
   public static final int PARENT_GROUP_ID_EMPTY = 1703;
  
   
   public static final int RESOURCE_ALREADY_EXISTS_ERROR = 2;
}
