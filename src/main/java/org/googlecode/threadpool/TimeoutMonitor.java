package org.googlecode.threadpool;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.googlecode.threadpool.util.NamedThreadFactory;
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
				LOG.debug("Create TimeoutChecker. {} ", key);
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

	public static class TimeoutChecker implements Runnable {

		private static final Logger LOG = LoggerFactory
				.getLogger(TimeoutChecker.class);

		private static final String THREAD_NAME_PREFIX = "TimeoutCheckerThread-";

		private String taskKey;

		private DelayQueue<FutureTaskDelay> taskDelayQueue;

		private ExecutorService exec;

		/**
		 * @param taskKey
		 */
		private TimeoutChecker(String taskKey) {
			this.taskKey = taskKey;
			this.taskDelayQueue = new DelayQueue<FutureTaskDelay>();
			this.exec = Executors
					.newSingleThreadExecutor(new NamedThreadFactory(
							THREAD_NAME_PREFIX + taskKey, false));
		}

		public void start() {
			LOG.debug("TimeoutChecker Start.");
			exec.execute(this);
		}

		public void stop() {
			taskDelayQueue.clear();
			exec.shutdownNow();
		}

		public FutureTaskDelay addTask(RunnableTask task) {
			FutureTaskDelay futureTaskDelay = FutureTaskDelay.newInstance(task);
			taskDelayQueue.add(futureTaskDelay);
			return futureTaskDelay;
		}

		public void cancelTask(RunnableTask task) {
			taskDelayQueue.remove(FutureTaskDelay.newInstance(task));
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					LOG.debug("Timeout,TaskKey: {} ", getTaskKey());
					FutureTaskDelay task = taskDelayQueue.take();
					task.cancel(true);
				} catch (InterruptedException e) {
				}
			}
		}

		public DelayQueue<FutureTaskDelay> getTaskDelayQueue() {
			return taskDelayQueue;
		}

		public void setTaskDelayQueue(DelayQueue<FutureTaskDelay> taskDelayQueue) {
			this.taskDelayQueue = taskDelayQueue;
		}

		public String getTaskKey() {
			return taskKey;
		}

		public void setTaskKey(String taskKey) {
			this.taskKey = taskKey;
		}

		public static TimeoutChecker newInstance(String taskKey) {
			return new TimeoutChecker(taskKey);
		}

		@Override
		public String toString() {
			return "TimeoutChecker [taskDelayQueue=" + taskDelayQueue
					+ ", taskKey=" + taskKey + "]";
		}

	}
}
