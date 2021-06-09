package com.fintellix.platformcore.common.exception;

/**
 * @author angshu
 */
public class VyasaException extends Exception {

    private static final long serialVersionUID = 33227657912667118L;

    /**
     * Default Constructor
     */
    public VyasaException() {
        super();
    }

    /**
     * Constructor
     *
     * @param errMsg
     * @param te
     */
    public VyasaException(String errMsg, Throwable te) {
        super(errMsg, te);
    }

    /**
     * @param errMsg
     */
    public VyasaException(String errMsg) {
        super(errMsg);
    }

    /**
     * Constructor
     *
     * @param te
     */
    public VyasaException(Throwable te) {
        super(te);
    }
}
