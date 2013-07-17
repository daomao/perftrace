package org.googlecode.perftrace.filter;

import org.googlecode.perftrace.Log4JPerftrace;
import org.googlecode.perftrace.PerfTrace;
import org.googlecode.perftrace.perf4j.Log4JStopWatch;
import org.googlecode.perftrace.perf4j.LoggingStopWatch;

/**
 * @author zhongfeng
 *
 */
public class Log4JPerftraceFilter extends PerftraceFilter{

	@Override
	protected LoggingStopWatch createLoggingStopWatch() {	
		return new Log4JStopWatch(ROOT_TOTAL_WATCH, true);
	}

	@Override
	protected PerfTrace getPerftrace() {
		return Log4JPerftrace.getInstance();
	}

}
