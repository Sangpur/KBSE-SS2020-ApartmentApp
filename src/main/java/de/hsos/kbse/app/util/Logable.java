/*
 * ANNOTATION Logable
 * 
 */
package de.hsos.kbse.app.util;

import de.hsos.kbse.app.enums.LogLevel;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

/**
 *
 * @author Annika Limbrock
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.CONSTRUCTOR})
@Inherited
public @interface Logable {
    
    /* Die Annotation @Nonbinding sorgt dafuer, dass alle Loglevels von der gleichen LogInterceptor-Funktion behandelt werden. Entfernt man 
     * diese Annotation muesste es mehrere Interceptors geben - fuer jedes Loglevel einen eigenen. */
    @Nonbinding 
    LogLevel value() default LogLevel.FINEST;
    
}
