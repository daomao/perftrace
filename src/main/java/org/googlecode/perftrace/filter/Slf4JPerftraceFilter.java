package org.googlecode.perftrace.filter;

import org.googlecode.perftrace.PerfTrace;
import org.googlecode.perftrace.Slf4JPerftrace;
import org.googlecode.perftrace.perf4j.LoggingStopWatch;
import org.googlecode.perftrace.perf4j.Slf4JStopWatch;

/**
 * @author zhongfeng
 *
 */
public class Slf4JPerftraceFilter extends PerftraceFilter{

	@Override
	protected LoggingStopWatch createLoggingStopWatch() {	
		return new Slf4JStopWatch(ROOT_TOTAL_WATCH, true);
	}

	@Override
	protected PerfTrace getPerftrace() {
		return Slf4JPerftrace.getInstance();
	}

}
