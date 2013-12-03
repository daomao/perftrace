package org.googlecode.threadpool;

import java.util.Iterator;
import java.util.Map.Entry;
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
public class TimeoutMonitor {

	private final static TimeoutMonitor INSTANCE = new TimeoutMonitor();

	private final static Logger LOG = LoggerFactory
			.getLogger(TimeoutMonitor.class);

	private final LoadingCache<String, TimeoutChecker> timeoutCheckerCache;

	private TimeoutMonitor() {
		this.timeoutCheckerCache = initTimeoutCheckerCache();
	}

	private LoadingCache<String, TimeoutChecker> initTimeoutCheckerCache() {
		CacheLoader<String, TimeoutChecker> runStatsLoader = new CacheLoader<String, TimeoutChecker>() {
			@Override
			public TimeoutChecker load(String key) throws Exception {
				TimeoutChecker checker = TimeoutChecker.newInstance(key);
				checker.start();
				return checker;
			}
		};
		return CacheBuilder.newBuilder().build(runStatsLoader);
	}

	public FutureTaskDelay addTaskTimeoutMonitor(RunnableTask task) {
		TimeoutChecker checker = null;
		try {
			checker = getTimeoutCheckerCache().get(task.getTaskKey());
		} catch (ExecutionException e) {
			LOG.error("add task timeout error", e);
		}
		return checker.addTask(task);
	}

	public void cancelTaskTimeoutMonitor(RunnableTask task) {
		TimeoutChecker checker = null;
		try {
			checker = getTimeoutCheckerCache().get(task.getTaskKey());
		} catch (ExecutionException e) {
			LOG.error("cancel task timeout error", e);
		}
		checker.cancelTask(task);
	}

	public LoadingCache<String, TimeoutChecker> getTimeoutCheckerCache() {
		return timeoutCheckerCache;
	}

	public static TimeoutMonitor getInstance() {
		return INSTANCE;
	}

	public void shutdown() {
		Iterator<Entry<String, TimeoutChecker>> iter = getTimeoutCheckerCache()
				.asMap().entrySet().iterator();
		while (iter.hasNext()) {
			iter.next().getValue().stop();
		}
		getTimeoutCheckerCache().invalidateAll();
	}
}
