package org.googlecode.perftrace;

import org.apache.log4j.Logger;

/**
 * @author zhongfeng
 * 
 */
public class Log4JPerftrace extends PerfTrace {

	private final static Logger LOG4J_PERF_TRACE_LOG = Logger
			.getLogger(PerfTrace.PERF_TRACE_LOG);

	private final static PerfTrace INST = new Log4JPerftrace();

	private Log4JPerftrace() {
	}

	@Override
	public void log(String logSring) {
		LOG4J_PERF_TRACE_LOG.info(logSring);
	}

	public static PerfTrace getInstance() {
		return INST;
	}
}
