package org.googlecode.perftrace.filter;

import org.googlecode.perftrace.JavaLogPerftrace;
import org.googlecode.perftrace.PerfTrace;
import org.googlecode.perftrace.perf4j.JavaLogStopWatch;
import org.googlecode.perftrace.perf4j.LoggingStopWatch;

/**
 * @author zhongfeng
 *
 */
public class JavaLogPerftraceFilter extends PerftraceFilter{

	@Override
	protected LoggingStopWatch createLoggingStopWatch() {	
		return new JavaLogStopWatch(ROOT_TOTAL_WATCH, true);
	}

	@Override
	protected PerfTrace getPerftrace() {
		return JavaLogPerftrace.getInstance();
	}

}
