package org.googlecode.threadpool;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

import org.googlecode.threadpool.PoolConfig.TaskConfig.TaskConfigBuilder;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * @author zhongfeng
 * 
 */
public class PoolConfig {

	public final static PoolConfig DEFAULT_CONFIG = new PoolConfig(500, 1);

	private LoadingCache<String, TaskConfig> TASK_CONFIG_CACHE = CacheBuilder
			.newBuilder().build(new CacheLoader<String, TaskConfig>() {
				@Override
				public TaskConfig load(String key) throws Exception {
					return TaskConfigBuilder.newInstance(key).build();
				}
			});

	private int maximumPoolSize ;

	private int minAvailableSharedPoolSize;


	/**
	 * 
	 */
	public PoolConfig() {
		this(500,1);
	}

	/**
	 * @param maximumPoolSize
	 * @param minAvailableSharedPoolSize
	 */
	public PoolConfig(int maximumPoolSize, int minAvailableSharedPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
		this.minAvailableSharedPoolSize = minAvailableSharedPoolSize;
	}

	public void addTaskConfig(TaskConfig taskCfg) {
		TASK_CONFIG_CACHE.put(taskCfg.getTaskKey(), taskCfg);
	}

	public int getMinAvailableSharedPoolSize() {
		return minAvailableSharedPoolSize;
	}

	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public void setMinAvailableSharedPoolSize(int value) {
		minAvailableSharedPoolSize = value;
	}

	public void setMaximumPoolSize(int value) {
		maximumPoolSize = value;
	}

	public Collection<TaskConfig> getAllTaskConfig() {
		return TASK_CONFIG_CACHE.asMap().values();
	}

	public TaskConfig getTaskConfig(String taskKey) {
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
		/**
		 * 
		 */
		private final int reserve;

		/**
		 * 
		 */
		private final int elastic;

		/**
		 * 
		 */
		private final int bufferSize;

		/**
		 * 
		 */
		private final String taskKey;
		
		private final long timeout;

		private TaskConfig(TaskConfigBuilder builder) {
			this.reserve = builder.reserve;
			this.elastic = builder.elastic;
			this.bufferSize = builder.bufferSize;
			this.taskKey = builder.taskKey;
			this.timeout = builder.timeout;
		}

		public int getReserve() {
			return reserve;
		}

		public int getElastic() {
			return elastic;
		}


		public int getBufferSize() {
			return bufferSize;
		}


		public String getTaskKey() {
			return taskKey;
		}


		
		public long getTimeout() {
			return timeout;
		}

		@Override
		public String toString() {
			return "TaskConfig [bufferSize=" + bufferSize + ", elastic="
					+ elastic + ", reserve=" + reserve + ", taskKey=" + taskKey
					+ ", timeout=" + timeout + "]";
		}

		public static class TaskConfigBuilder
		{
			/**
			 * 
			 */
			private final String taskKey;

			private static final int DEFAULT_BUFFER_SIZE = 100;

			/**
			 * 
			 */
			private int reserve = 0;

			/**
			 * 
			 */
			private int elastic = Integer.MAX_VALUE;

			/**
			 * 
			 */
			private int bufferSize = DEFAULT_BUFFER_SIZE;
			
			private long timeout = -1L;

			/**
			 * @param taskKey
			 */
			public TaskConfigBuilder(String taskKey) {
				this.taskKey = taskKey;
			}
			
			public TaskConfigBuilder reserve(int reserve)
			{
				this.reserve = reserve;
				return this;
			}
			
			public TaskConfigBuilder elastic(int elastic)
			{
				this.elastic = elastic;
				return this;
			}
			public TaskConfigBuilder bufferSize(int bufferSize)
			{
				this.bufferSize = bufferSize;
				return this;
			}
			
			public TaskConfigBuilder timeout(long timeout)
			{
				this.timeout = timeout;
				return this;
			}
			
			public static TaskConfigBuilder newInstance(String taskKey)
			{
				return new TaskConfigBuilder(taskKey);
			}
			public TaskConfig build(){
				return new TaskConfig(this);
			}
		}

	}

}
