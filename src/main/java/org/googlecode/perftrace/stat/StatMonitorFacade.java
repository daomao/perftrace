package org.googlecode.perftrace.stat;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * @author zhongfeng
 * 
 */
public final class StatMonitorFacade {

	private static final String TOTAL_SERVICE_NAME = "Total";

	public final static StatMonitorFacade MONITOR = new StatMonitorFacade();

	private final static Logger LOG = LoggerFactory
			.getLogger(StatMonitorFacade.class);

	/**
	 * key :serviceName
	 */
	private LoadingCache<String, StatMonitor> statCache;

	private StatMonitorFacade() {
		this.statCache = initStatCache();
	}

	private LoadingCache<String, StatMonitor> initStatCache() {
		CacheLoader<String, StatMonitor> runStatsLoader = new CacheLoader<String, StatMonitor>() {
			@Override
			public StatMonitor load(String key) throws Exception {
				return new StatMonitor(key);
			}
		};

		return CacheBuilder.newBuilder().build(runStatsLoader);
	}

	public static StatMonitorFacade getInstance() {
		return MONITOR;
	}

	private LoadingCache<String, StatMonitor> getStatCache() {
		return statCache;
	}

	public final static void startTotal() {
		start(TOTAL_SERVICE_NAME);
	}

	public final static void stopTotal(long elapsedTime,
			boolean isFault) {
		stop(TOTAL_SERVICE_NAME, elapsedTime, isFault);
	}
	
	public final static void start(String serviceName) {
		StatMonitor statMgr = getStatManager(serviceName);
		if (statMgr != null)
			statMgr.start();
	}

	public final static void stop(String serviceName, long elapsedTime,
			boolean isFault) {
		StatMonitor statMgr = getStatManager(serviceName);
		if (statMgr != null)
			statMgr.stop(elapsedTime, isFault);
	}

	/**
	 * @param serviceName
	 * @return
	 * @throws ExecutionException
	 */
	private static StatMonitor getStatManager(String serviceName) {
		try {
			return MONITOR.getStatCache().get(serviceName);
		} catch (ExecutionException e) {
			LOG.error("", e);
			return null;
		}
	}
}
