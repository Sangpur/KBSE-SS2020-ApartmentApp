/*
 * CLASS LogInterceptor
 * Erzeugung des Interceptors: Hierfuer wird eine neue Klasse erstellt. Diese wird mit der zuvor erzeugten @Logable Annotation versehen.
 */
package de.hsos.kbse.app.util;

import de.hsos.kbse.app.enums.LogLevel;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import javax.inject.Inject;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 *
 * @author Annika Limbrock
 */
@Interceptor @Logable
public class LogInterceptor implements Serializable  {
    
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    private LogService logger;
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @Inject
    public LogInterceptor(LogService logger) {
        this.logger = logger;
    }
    
    @AroundConstruct
    public Object logConstructorCall(InvocationContext ctx) throws Exception {
        /* Zuerst wird hier der Name der Klasse und der Methode abgefragt, die mit der Annotation @Logable versehen wurden. */
        String declaringClass = ctx.getConstructor().getDeclaringClass().getName();
        String constructor = ctx.getConstructor().getName();
        /* Im Folgenden wird ueberprueft, ob die Annotation auf Klassen- oder Methodenebene zu finden ist. Falls also keine Annotation an die
         * Klasse gehaengt ist, wird diese in der if-Abfrage von der Methode abgefragt - ansonsten aus der Klasse. */
        Annotation classAnnotation = ctx.getConstructor().getDeclaringClass().getAnnotation(Logable.class);
        LogLevel logLevel;
        if(classAnnotation != null) {
            logLevel = ctx.getConstructor().getDeclaringClass().getAnnotation(Logable.class).value();
        } else {
            logLevel = ctx.getConstructor().getAnnotation(Logable.class).value();
        }
        /* Aufruf des LogManagers zur Generierung der Nachricht */
        logger.log(declaringClass,logLevel,"Construct " + constructor);
        Object result = ctx.proceed();
        return result;
    }
    
    /* Mit @AroundInvoke wird ein Aspekt an alle Methoden gesetzt, die mit der Annotation @Logable versehen sind. In diesem Beispiel
     * Die eigentliche Methode wird mit ctx.proceed() ausgefuehrt. */
    @AroundInvoke
    public Object logMethodCall(InvocationContext ctx) throws Exception {
        /* Zuerst wird hier der Name der Klasse und der Methode abgefragt, die mit der Annotation @Logable versehen wurden. */
        String declaringClass = ctx.getMethod().getDeclaringClass().getName();
        String method = ctx.getMethod().getName();
        /* Im Folgenden wird ueberprueft, ob die Annotation auf Klassen- oder Methodenebene zu finden ist. Falls also keine Annotation an die
         * Klasse gehaengt ist, wird diese in der if-Abfrage von der Methode abgefragt - ansonsten aus der Klasse. */
        Annotation classAnnotation = ctx.getMethod().getDeclaringClass().getAnnotation(Logable.class);
        LogLevel logLevel;
        if(classAnnotation != null) {
            logLevel = ctx.getMethod().getDeclaringClass().getAnnotation(Logable.class).value();
        } else {
            logLevel = ctx.getMethod().getAnnotation(Logable.class).value();
        }
        /* Aufruf des LogManagers zur Generierung der Nachricht */
        logger.log(declaringClass,logLevel,method + " started");
        Object result = ctx.proceed();
        logger.log(declaringClass,logLevel,method + " ended");
        return result;
    }
    
}
