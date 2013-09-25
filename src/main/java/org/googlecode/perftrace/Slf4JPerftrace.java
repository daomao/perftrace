package org.googlecode.perftrace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhongfeng
 * 
 */
public class Slf4JPerftrace extends PerfTrace {

	private final static Logger SLF4J_PERF_TRACE_LOG = LoggerFactory
			.getLogger(PerfTrace.PERF_TRACE_LOG);

	private final static PerfTrace INST = new Slf4JPerftrace();

	private Slf4JPerftrace() {
	}

	@Override
	public void log(String logSring) {
		SLF4J_PERF_TRACE_LOG.info(logSring);
	}

	public static PerfTrace getInstance() {
		return INST;
	}
	
	public static void main(String[] args)
	{
		org.googlecode.perftrace.log4j.Logger logger = org.googlecode.perftrace.log4j.Logger.getLogger("123");
		logger.error("123");
	}
}
