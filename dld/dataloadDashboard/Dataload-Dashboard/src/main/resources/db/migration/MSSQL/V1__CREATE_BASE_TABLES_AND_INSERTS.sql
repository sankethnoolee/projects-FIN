SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET ANSI_PADDING ON
GO

CREATE TABLE CLIENT_MASTER 
(
CLIENT_CODE					VARCHAR(100),
CLIENT_DESCRIPTION 			VARCHAR(256),
CONSTRAINT CLIENT_CODE_PK PRIMARY KEY (CLIENT_CODE)
);

CREATE TABLE DIM_DATA_SOURCE
(
  DATA_SOURCE_ID           INTEGER              NOT NULL,
  DATA_SOURCE_NAME         VARCHAR(128),
  IS_SCGL_IND              CHAR(1),
  SCGL_CURRENCY_UCID       CHAR(20),
  FINANCIAL_YEAR_TYPE_ID   INTEGER,
  AUDIT_GL_DATA_SOURCE_ID  INTEGER,
  CLIENT_CODE              VARCHAR(100),
  CONSTRAINT PK_DIMDATASOURCE PRIMARY KEY (DATA_SOURCE_ID, CLIENT_CODE)
);

CREATE TABLE DL_ALL_TASK_LOG
(
  SEQUENCE_NUMBER  			INTEGER,
  REQUEST_CONTENT  			VARCHAR(4000),
  REQUEST_ORIGIN   			VARCHAR(1000)
);

CREATE TABLE DL_ENTITY
(
CLIENT_CODE 				VARCHAR(100),
OWNER_NAME  				VARCHAR(100),
ENTITY_NAME  				VARCHAR(1000),
ENTITY_TYPE  				VARCHAR(100),
ENTITY_DETAIL  				VARCHAR(256),
ENTITY_DESCRIPTION  		VARCHAR(256),
CONSTRAINT DL_ENTITY_PK PRIMARY KEY (CLIENT_CODE,OWNER_NAME,ENTITY_NAME)
);

CREATE TABLE DL_ENTITY_OWNER
(
CLIENT_CODE 				VARCHAR(100),
OWNER_NAME  				VARCHAR(100),
OWNER_DESCRIPTION  			VARCHAR(256),
EXTERNAL_SOURCE_INDICATOR   CHAR(1),
DATA_SOURCE_ID  			INTEGER,
SOLUTION_ID  				INTEGER,
DISPLAY_ORDER  				INTEGER,
CONTACT_DETAILS  			VARCHAR(2000),
CONSTRAINT DL_ENTITY_OWNER_PK PRIMARY KEY (CLIENT_CODE,OWNER_NAME)
);

CREATE TABLE DL_ENTITY_TYPE 
(
  ENTITY_TYPE              VARCHAR(100),
  ENTITY_TYPE_DESCRIPTION  VARCHAR(256),
  CLIENT_CODE              VARCHAR(100),
  CONSTRAINT ENTITY_TYPE_PK PRIMARY KEY (ENTITY_TYPE,CLIENT_CODE)
);


CREATE TABLE DL_FLOW_TYPE 
(
CLIENT_CODE 				VARCHAR(100),
FLOW_TYPE 					VARCHAR(100),
FLOW_DESCRIPTION			VARCHAR(256),
CONSTRAINT DL_FLOW_TYPE_PK PRIMARY KEY (CLIENT_CODE,FLOW_TYPE)
);


CREATE TABLE DL_TASK_EXECUTION_LOG
(
SEQUENCE_NUMBER 			INTEGER,
CLIENT_CODE  				VARCHAR(100),
TASK_REPOSITORY  			VARCHAR(100),
TASK_NAME 					VARCHAR(1000),
FLOW_TYPE  					VARCHAR(100),
FLOW_SEQUENCE_NO  			VARCHAR(100),
TECHNICAL_TASK_NAME  		VARCHAR(1000),
TECHNICAL_SUB_TASK_NAME  	VARCHAR(1000),
TASK_STATUS  				VARCHAR(100),
RUN_PERIOD_DATE  			DATETIME,
BUSINESS_PERIOD_DATE    		DATETIME,
RUN_DETAILS  				VARCHAR(4000),
START_DATE_TIME  			DATETIME,
END_DATE_TIME  				DATETIME,
SOURCE_COUNT  				INTEGER,
TARGET_COUNT  				INTEGER,
TARGET_INSERTED_COUNT  		INTEGER,
TARGET_UPDATED_COUNT  		INTEGER,
TARGET_REJECTED_RECORD  	INTEGER,
CONSTRAINT DL_TASK_EXECUTION_LOG_PK PRIMARY KEY (SEQUENCE_NUMBER)
);


CREATE TABLE DL_TASK_FLOW_TYPE
(
CLIENT_CODE 				VARCHAR(100),
TASK_REPOSITORY  			VARCHAR(100),
TASK_NAME  					VARCHAR(1000),
FLOW_TYPE  					VARCHAR(100),
VERSION_NO      			INTEGER,
CONSTRAINT DL_TASK_FLOW_TYPE_PK PRIMARY KEY (CLIENT_CODE,TASK_REPOSITORY,FLOW_TYPE,TASK_NAME,VERSION_NO)
);

CREATE TABLE DL_TASK_FREQUENCY
(
CLIENT_CODE 				VARCHAR(100),
TASK_REPOSITORY  			VARCHAR(100),
TASK_NAME  					VARCHAR(1000),
FREQUENCY  					VARCHAR(100),
OFFSET  					INTEGER,
IS_EXCLUSION_INDICATOR  	CHAR(1),
VERSION_NO              	INTEGER,
CONSTRAINT DL_TASK_FREQUENCY_PK PRIMARY KEY (CLIENT_CODE,TASK_REPOSITORY,TASK_NAME,FREQUENCY,OFFSET,IS_EXCLUSION_INDICATOR,VERSION_NO)
);


CREATE TABLE DL_TASK_MASTER
(
  CLIENT_CODE              VARCHAR(100),
  TASK_REPOSITORY          VARCHAR(100),
  TASK_NAME                VARCHAR(1000),
  TASK_TYPE                VARCHAR(100),
  TASK_DESCRIPTION         VARCHAR(256),
  TECHNICAL_TASK_NAME      VARCHAR(1000),
  TECHNICAL_SUB_TASK_NAME  VARCHAR(1000),
  IS_ACTIVE                CHAR(1),
  VERSION_NO               INTEGER,
  EFFECTIVE_START_DATE     DATETIME,
  EFFECTIVE_END_DATE       DATETIME,
  IS_VALIDATION_REQUIRED   CHAR(1),
CONSTRAINT DL_TASK_MASTER_PK PRIMARY KEY (CLIENT_CODE, TASK_REPOSITORY, TASK_NAME, VERSION_NO)
);

CREATE TABLE DL_TASK_REPOSITORY
(
  CLIENT_CODE             VARCHAR(100),
  REPOSITORY_NAME         VARCHAR(100),
  REPOSITORY_DESCRIPTION  VARCHAR(256)
);

CREATE TABLE DL_TASK_SOURCE_TARGET
(
  CLIENT_CODE     		 VARCHAR(100)           NOT NULL,
  TASK_REPOSITORY  		 VARCHAR(100)           NOT NULL,
  TASK_NAME        		 VARCHAR(1000)          NOT NULL,
  OWNER_NAME      		 VARCHAR(100)           NOT NULL,
  ENTITY_NAME     		 VARCHAR(1000)          NOT NULL,
  LINK_TYPE       		 CHAR(1)                 NOT NULL,
  VERSION_NO     		 INTEGER,
CONSTRAINT DL_TASK_SOURCE_TARGET_PK PRIMARY KEY (CLIENT_CODE, TASK_REPOSITORY, TASK_NAME, OWNER_NAME, ENTITY_NAME, LINK_TYPE, VERSION_NO)
);

CREATE TABLE DL_TASK_TYPE
(
  TASK_TYPE              VARCHAR(100),
  TASK_TYPE_DESCRIPTION  VARCHAR(256),
  IS_EXECUTABLE_TASK     CHAR(1),
  CLIENT_CODE            VARCHAR(100),
  CONSTRAINT DL_TASK_TYPE_PK PRIMARY KEY (TASK_TYPE, CLIENT_CODE)
);



CREATE TABLE DLD_USERS
(
  USERID      			 INTEGER,
  USERNAME   			 VARCHAR(30),
  PASSWORD    			 VARCHAR(64),
  CLIENT_CODE 			 VARCHAR(20),
  CONSTRAINT DLD_USERS_PK PRIMARY KEY (USERID)
);

CREATE TABLE VYASASOLUTION
(
  SOLUTIONID          	INTEGER                  NOT NULL,
  PRODUCTID           	INTEGER                  NOT NULL,
  SOLUTIONNAME        	VARCHAR(50),
  SOLUTIONDESCRIPTION 	VARCHAR(50),
  BELONGSTO            	VARCHAR(10),
  ISACTIVE             	INTEGER,
  BITYPE               	VARCHAR(10),
  ISSFACTIVE          	INTEGER                  DEFAULT 0,
  CLIENT_CODE         	VARCHAR(100),
  CONSTRAINT VYASASOLUTION_PK PRIMARY KEY (SOLUTIONID, CLIENT_CODE)
);

Insert into CLIENT_MASTER
   (CLIENT_CODE, CLIENT_DESCRIPTION)
 Values
   ('PLT', 'Fintellix Platform');
   

Insert into DLD_USERS
   (USERID, USERNAME, PASSWORD, CLIENT_CODE)
 Values
   (0, 'admin', '$2a$12$YNKjX7WYUoialDEVYf7kbe474BJEql7RWxCZOI8SET9kF1frnxLp6', 'PLT');
COMMIT;