package org.googlecode.perftrace;

import java.lang.annotation.Annotation;

import org.googlecode.perftrace.perf4j.StopWatch;

/**
 * 
 * This unusual concrete implementation of this Profiled annotation interface is used for cases where some
 * interception frameworks may want to profile methods that DON'T have a profiled annotation (for example, EJB 3.0
 * interceptors). See the code for {@link org.perf4j.aop.AbstractEjbTimingAspect} for an example of how this is
 * used.
 * 
 */
@SuppressWarnings("all")
public class DefaultGProfiled implements GProfiled {
 
    private long timeThreshold;
    
    private DefaultGProfiled() { this.timeThreshold = 0L;}
    
    private DefaultGProfiled(long timeThreshold) { this.timeThreshold = timeThreshold;}

    public String tag() { return DEFAULT_TAG_NAME; }

    public String message() { return ""; }

    public String logger() { return StopWatch.DEFAULT_LOGGER_NAME; }

    public String level() { return "INFO"; }

    public boolean el() { return true; }

    public boolean logFailuresSeparately() { return true; }

    public long timeThreshold() { return this.timeThreshold; }
    
    public boolean normalAndSlowSuffixesEnabled() { return true; }
    
    public Class<? extends Annotation> annotationType() { return getClass(); }
    
    public static GProfiled getInstance(long timeThreshold){
    	return new DefaultGProfiled(timeThreshold);
    }
    
    public static GProfiled getInstance(){
    	return new DefaultGProfiled();
    }
}
