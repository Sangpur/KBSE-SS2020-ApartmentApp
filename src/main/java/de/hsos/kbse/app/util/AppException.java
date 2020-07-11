/*
 * CHECKED EXCEPTION AppException
 *
 */
package de.hsos.kbse.app.util;

/**
 *
 * @author Annika Limbrock
 */
public class AppException extends Exception {
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    public AppException() {
        super("Eine AppException ist aufgetreten!");
    }

    public AppException(String msg){
        super(msg);
    }
    
}
