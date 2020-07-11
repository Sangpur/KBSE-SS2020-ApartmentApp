/*
 * ENUM LogLevel
 * 
 */
package de.hsos.kbse.app.enums;

/**
 *
 * @author Annika Limbrock
 */
public enum LogLevel {
    
    /* Bereits vorhandene Level aus java.util.logging.Level */
    SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST;
    
    @Override
    public String toString() {
        return super.toString();
    }
    
}
