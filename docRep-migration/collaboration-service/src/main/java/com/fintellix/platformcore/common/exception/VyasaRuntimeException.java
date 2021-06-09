/*********************************************************************************
 * TODO DESCRIPTION: 
 * Created on Sep 8, 2006
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
public class VyasaRuntimeException extends RuntimeException {

    /**
     * Default Constructor
     */
    public VyasaRuntimeException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructor
     * @param errMsg
     */
    public VyasaRuntimeException(String errMsg) {
        super(errMsg);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructor
     * @param errMsg
     * @param te
     */
    public VyasaRuntimeException(String errMsg, Throwable te) {
        super(errMsg, te);
    }

    /**
     * Constructor
     * @param te
     */
    public VyasaRuntimeException(Throwable te) {
        super(te);
    }

}
