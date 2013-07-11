package org.googlecode.perftrace.perf4j;

import java.io.Serializable;
import java.util.concurrent.Callable;


/**
 * This helper wrapper class can be used to add timing statements to an existing Callable instance, logging how long
 * it takes for the call method to execute. Note that instances of this class are only serializable if the wrapped
 * Callable is serializable.
 *
 */
public class TimedCallable<V> implements Callable<V>, Serializable {
    private static final long serialVersionUID = -7581382177897573004L;
    private Callable<V> wrappedTask;
    private LoggingStopWatch stopWatch;

    /**
     * Wraps the existing Callable in order to time its call method.
     *
     * @param task      The existing Callable whose call method is to be timed and executed. May not be null.
     * @param stopWatch The LoggingStopWatch to use to time the call method execution. Note that this stop watch should
     *                  already have its tag and message set to what should be logged when the task is run. May not
     *                  be null.
     */
    public TimedCallable(Callable<V> task, LoggingStopWatch stopWatch) {
        this.wrappedTask = task;
        this.stopWatch = stopWatch;
    }

    /**
     * Gets the Callable task that is wrapped by this TimedCallable.
     *
     * @return The wrapped Callable whose execution time is to be logged.
     */
    public Callable<V> getWrappedTask() {
        return wrappedTask;
    }

    /**
     * Gets the LoggingStopWatch that will be used to time the call method execution.
     *
     * @return The LoggingStopWatch to use to log execution time.
     */
    public LoggingStopWatch getStopWatch() {
        return stopWatch;
    }

    /**
     * Executes the call method of the underlying task, using the LoggingStopWatch to track the execution time.
     */
    public V call() throws Exception {
        try {
            stopWatch.start();
            return wrappedTask.call();
        } finally {
            stopWatch.stop();
        }
    }
}
