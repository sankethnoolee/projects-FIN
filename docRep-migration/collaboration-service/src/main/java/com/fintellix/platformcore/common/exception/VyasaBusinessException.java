/*********************************************************************************
 * TODO DESCRIPTION: 
 * Created on Aug 8, 2006
 * Copyright (C) 2006 i-Create Software India Pvt Ltd. All Rights Reserved.
 ********************************************************************************/
/**
 * 
 */
package com.fintellix.platformcore.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author angshu
 *
 */
public class VyasaBusinessException extends VyasaException {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

    private int errorCode = -1;
    private static final long serialVersionUID = 1L;

    /**
     * Constrctor
     * @param errorCode
     */
    public VyasaBusinessException(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Constrctor
     * @param errorCode,te
     */
    public VyasaBusinessException(int errorCode, Throwable te) {
    	super(te);
    	this.errorCode = errorCode;
    }

    /**
     * @return Returns the errorCode.
     */
    public int getErrorCode() {
        return errorCode;
    }
    /**
     * @param errorCode The errorCode to set.
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
    /**
     * 
     */
    public VyasaBusinessException() {
        super();
        // TODO Auto-generated constructor stub
    }
    /**
     * @param errMsg
     * @param te
     */
    public VyasaBusinessException(String errMsg, Throwable te) {
        super(errMsg, te);
        // TODO Auto-generated constructor stub
    }
    
    
    /**
     * @param errMsg
     */
    public VyasaBusinessException(String errMsg) {
        super(errMsg);
        // TODO Auto-generated constructor stub
    }
    /**
     * @param te
     */
    public VyasaBusinessException(Throwable te) {
        super(te);
        // TODO Auto-generated constructor stub
    }
    
    

}
