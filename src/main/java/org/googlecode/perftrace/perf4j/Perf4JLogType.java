package org.googlecode.perftrace.perf4j;

import java.util.HashMap;
import java.util.Map;

import org.googlecode.perftrace.util.StringUtils;

/**
 * @author zhongfeng
 * 
 */
public enum Perf4JLogType {

	/**
	 * JAVA JDK 提供的log
	 */
	JDKLOG,

	/**
	 *  
	 */
	SLF4J,

	/**
	 * 
	 */
	LOG4J;
	private final static Map<Perf4JLogType, String> LOGTYPE_LOGWATCH_MAP = new HashMap<Perf4JLogType, String>();

	static {
		LOGTYPE_LOGWATCH_MAP.put(SLF4J, "org.googlecode.perftrace.perf4j.Slf4JStopWatch");
		LOGTYPE_LOGWATCH_MAP.put(LOG4J, "org.googlecode.perftrace.perf4j.Log4JStopWatch");
		LOGTYPE_LOGWATCH_MAP.put(JDKLOG, "org.googlecode.perftrace.perf4j.JavaLogStopWatch");
	}

	public static Perf4JLogType getPerf4JLogType(String type) {
		if (StringUtils.equalsIgnoreCase("SLF4J", type))
			return SLF4J;
		if (StringUtils.equalsIgnoreCase("LOG4J", type))
			return LOG4J;
		return JDKLOG;
	}

	public static Perf4JLogType getDefaultPerf4JLogType() {
		return JDKLOG;
	}

	public static String getLogStopWatchClass(Perf4JLogType perf4jLogType) {
		return LOGTYPE_LOGWATCH_MAP.get(perf4jLogType);
	}

}
