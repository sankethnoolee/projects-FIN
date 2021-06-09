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
public class AuditException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     */
    public AuditException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructor
     * @param errMsg
     * @param te
     */
    public AuditException(String errMsg, Throwable te) {
        super(errMsg, te);
    }

    /**
     * @param arg0
     */
    public AuditException(String errMsg) {
        super(errMsg);
    }

    /**
     * Constructor
     * @param te
     */
    public AuditException(Throwable te) {
        super(te);
    }

   

    

}
