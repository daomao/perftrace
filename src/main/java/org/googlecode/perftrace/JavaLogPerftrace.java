package org.googlecode.perftrace;

import java.util.logging.Logger;

/**
 * @author zhongfeng
 * 
 */
public class JavaLogPerftrace extends PerfTrace {

	private final static Logger JAVALOG_PERF_TRACE_LOG = Logger
			.getLogger(PerfTrace.PERF_TRACE_LOG);

	private final static PerfTrace INST = new JavaLogPerftrace();

	private JavaLogPerftrace() {
	}

	@Override
	public void log(String logSring) {
		JAVALOG_PERF_TRACE_LOG.info(logSring);
	}

	public static PerfTrace getInstance() {
		return INST;
	}
}
