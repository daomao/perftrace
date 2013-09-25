package org.googlecode.perftrace;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.googlecode.perftrace.perf4j.LoggingStopWatch;
import org.googlecode.perftrace.schema.BootstrapPerftrace;
import org.googlecode.perftrace.util.StringUtils;

/**
 * @author zhongfeng
 * 
 */
public abstract class PerfTrace {

	private final static Logger logger = Logger.getLogger(PerfTrace.class
			.getName());

	public final static String PERF_TRACE_LOG = "org.googlecode.perftrace";

	private final static String PERF_LOG_SEG = "*";

	private final static String OUTPUT_LOG_SEG = "|";

	private static final ThreadLocal<ArrayList<LoggingStopWatch>> WATCH_CHAIN = new ThreadLocal<ArrayList<LoggingStopWatch>>() {
		public ArrayList<LoggingStopWatch> initialValue() {
			return new ArrayList<LoggingStopWatch>();
		}
	};

	// private static final

	private static final ThreadLocal<ArrayList<String>> ADDITION_MSG = new ThreadLocal<ArrayList<String>>() {
		public ArrayList<String> initialValue() {
			return new ArrayList<String>();
		}
	};

	public void addPerfWatch(LoggingStopWatch watch) {
		logger.log(Level.FINE, "Enter Add:{},watch :{}", new Object[] {
				watch.toString(), watch.isRootMethod() });
		if (isInsertPerfQueue(watch)) {
			getOtaPerfWatchList().add(watch);
		}
	}

	/**
	 * @param watch
	 * @return
	 * 
	 */
	private static boolean isInsertPerfQueue(LoggingStopWatch watch) {
		return (!getOtaPerfWatchList().isEmpty()) || watch.isRootMethod();
	}

	public void addAdditionMsg(String msg) {
		getAdditionMsg().add(msg);
	}

	public void watchStop(LoggingStopWatch watch) {
		logger.log(Level.FINE, "Enter Stop:{}", watch.toString());
		if (isStop(watch)) {
			doPerfLog();
			WATCH_CHAIN.remove();
			ADDITION_MSG.remove();
			return;
		}
		// 注意，这里需要有防止内存暴涨的保护措施，比如WATCH_CHAIN > 2000时，需要强制刷掉

		if (WATCH_CHAIN.get().size() > BootstrapPerftrace.getGlobal()
				.getMaxWatchChainDepth()) {
			logger.log(Level.WARNING,
					"WatchChain size is:{0},gt MaxWatchChainDepth:{1}",
					new Object[] {
							WATCH_CHAIN.get().size(),
							BootstrapPerftrace.getGlobal()
									.getMaxWatchChainDepth() });
			doPerfLog();
			WATCH_CHAIN.remove();
		}
		if (ADDITION_MSG.get().size() > BootstrapPerftrace.getGlobal()
				.getMaxAdditionMsgCount()) {
			logger.log(Level.WARNING,
					"ADDITION_MSG size is:{0},gt MaxAdditionMsgCount:{1}",
					new Object[] {
							ADDITION_MSG.get().size(),
							BootstrapPerftrace.getGlobal()
									.getMaxAdditionMsgCount() });
			doPerfLog();
			ADDITION_MSG.remove();
		}
	}

	/**
	 * @param watch
	 * @return
	 */
	private static boolean isStop(LoggingStopWatch watch) {
		return (!getOtaPerfWatchList().isEmpty())
				&& watch.equals(getOtaPerfWatchList().get(0));
	}

	private void doPerfLog() {
		String logString = buildOutputLogString();
		log(logString);
		// LogWatchComposite lwc = LogWatchParser.analysis(logString);
		// LOG.info(lwc.toXMLLogString());
	}

	/**
	 * abstract log method
	 * 
	 * @param logSring
	 */
	public abstract void log(String logSring);

	/**
	 * 
	 */
	private static String buildOutputLogString() {
		List<String> outputLogSegs = new ArrayList<String>();
		outputLogSegs.addAll(getAdditionMsg());
		outputLogSegs.add(buildPerfLogSeg());
		return StringUtils.join(outputLogSegs, OUTPUT_LOG_SEG);
	}

	/**
	 * @return
	 */
	private static String buildPerfLogSeg() {
		List<String> perfLogs = new ArrayList<String>();
		for (LoggingStopWatch watch : getOtaPerfWatchList()) {
			if (watch.isOverTimeThreshold())
				perfLogs.add(watch.getLogRetString());
		}
		String perfLogSeg = StringUtils.join(perfLogs, PERF_LOG_SEG);
		return perfLogSeg;
	}

	private static ArrayList<LoggingStopWatch> getOtaPerfWatchList() {
		return WATCH_CHAIN.get();
	}

	/**
	 * @return
	 */
	private static ArrayList<String> getAdditionMsg() {
		return ADDITION_MSG.get();
	}
}
