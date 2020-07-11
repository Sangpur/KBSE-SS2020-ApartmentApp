/*
 * CLASS LogService
 *
 */
package de.hsos.kbse.app.util;

import de.hsos.kbse.app.enums.LogLevel;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Annika Limbrock
 */
public class LogService implements Serializable {
    
    /* Erhaelt den Namen von der Klasse, in der die aufgerufe Funktion mit @Logable gekennzeichnet ist. */
    public void log(final String className, final LogLevel level, final String message) {
        Logger logger = Logger.getLogger(className);
        logger.log(Level.parse(level.toString()), message);
    }
    
}
