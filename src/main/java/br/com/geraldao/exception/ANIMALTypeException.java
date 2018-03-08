package br.com.geraldao.exception;

/**
 * Abnormal Not Identified Malformed Type Exception
 * 
 * @author yuri.campolongo
 */
public class ANIMALTypeException extends ClassCastException {

    private static final long serialVersionUID = -916197336605096101L;

    public ANIMALTypeException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

}
