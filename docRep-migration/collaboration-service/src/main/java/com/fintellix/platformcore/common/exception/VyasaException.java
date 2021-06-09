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
public class VyasaException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 33227657912667118L;

	/**
     * Default Constructor
     */
    public VyasaException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructor
     * @param errMsg
     * @param te
     */
    public VyasaException(String errMsg, Throwable te) {
        super(errMsg, te);
    }

    /**
     * @param arg0
     */
    public VyasaException(String errMsg) {
        super(errMsg);
    }

    /**
     * Constructor
     * @param te
     */
    public VyasaException(Throwable te) {
        super(te);
    }

   

    

}
