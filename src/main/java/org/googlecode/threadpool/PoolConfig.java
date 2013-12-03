package org.googlecode.threadpool;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * @author zhongfeng
 *
 */
public class PoolConfig {

	private final static LoadingCache<String, TaskConfig> TASK_CONFIG_CACHE = CacheBuilder
			.newBuilder().build(new CacheLoader<String, TaskConfig>() {
				@Override
				public TaskConfig load(String key) throws Exception {
					return new TaskConfig(key);
				}
			});

	private static int maximumPoolSize;

	private static int minAvailableSharedPoolSize;

	public static void addTaskConfig(TaskConfig taskCfg) {
		TASK_CONFIG_CACHE.put(taskCfg.getTaskKey(), taskCfg);
	}

	public static int getMinAvailableSharedPoolSize() {
		return minAvailableSharedPoolSize;
	}

	public static int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public static void setMinAvailableSharedPoolSize(int value) {
		minAvailableSharedPoolSize = value;
	}

	public static void setMaximumPoolSize(int value) {
		maximumPoolSize = value;
	}

	public static Collection<TaskConfig> getAllTaskConfig() {
		return TASK_CONFIG_CACHE.asMap().values();
	}

	public static TaskConfig getTaskConfig(String taskKey) {
		try {
			return TASK_CONFIG_CACHE.get(taskKey);
		} catch (ExecutionException e) {
		}
		return null;
	}

	/**
	 * @author zhongfeng
	 * 
	 */
	public static class TaskConfig {

		private static final String DEFAULT_TASK_KEY = "-DefaultTaskKey-";

		private static final int DEFAULT_BUFFER_SIZE = 100;

		public final static TaskConfig DEFAULT_CONFIG = new TaskConfig(
				DEFAULT_TASK_KEY);

		/**
		 * 
		 */
		private int reserve;

		/**
		 * 
		 */
		private int elastic;

		/**
		 * 
		 */
		private int bufferSize;

		/**
		 * 
		 */
		private String taskKey;

		/**
		 * 表示无限共享池里的线程
		 * 
		 * @param taskKey
		 */
		public TaskConfig(String taskKey) {
			this(0, Integer.MAX_VALUE, DEFAULT_BUFFER_SIZE, taskKey);
		}

		/**
		 * @param elastic
		 * @param taskKey
		 */
		public TaskConfig(int elastic, String taskKey) {
			this(0, elastic, DEFAULT_BUFFER_SIZE, taskKey);
		}

		/**
		 * @param reserve
		 * @param elastic
		 * @param taskKey
		 */
		public TaskConfig(int reserve, int elastic, String taskKey) {
			this(reserve, elastic, DEFAULT_BUFFER_SIZE, taskKey);
		}

		/**
		 * @param reserve
		 * @param elastic
		 * @param bufferSize
		 * @param taskKey
		 */
		public TaskConfig(int reserve, int elastic, int bufferSize,
				String taskKey) {
			this.reserve = reserve;
			this.elastic = elastic;
			this.bufferSize = bufferSize;
			this.taskKey = taskKey;
		}

		public int getReserve() {
			return reserve;
		}

		public void setReserve(int reserve) {
			this.reserve = reserve;
		}

		public int getElastic() {
			return elastic;
		}

		public void setElastic(int elastic) {
			this.elastic = elastic;
		}

		public int getBufferSize() {
			return bufferSize;
		}

		public void setBufferSize(int bufferSize) {
			this.bufferSize = bufferSize;
		}

		public String getTaskKey() {
			return taskKey;
		}

		public void setTaskKey(String taskKey) {
			this.taskKey = taskKey;
		}

		@Override
		public String toString() {
			return "TaskConfig [bufferSize=" + bufferSize + ", elastic="
					+ elastic + ", reserve=" + reserve + ", taskKey=" + taskKey
					+ "]";
		}

	}

}
