package org.googlecode.threadpool;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.googlecode.perftrace.stat.StatMonitor.SystemTimer;
import org.googlecode.threadpool.util.ThreadRenamingRunnable;

/**
 * @author zhongfeng
 * 
 */
public class RunnableTask implements Runnable {

	private final Runnable runnable;

	private final String taskKey;

	private final long timeout;

	private final long taskId;

	private final long expireTimeout;

	private volatile List<Quota> currentUsedQuota;

	private volatile boolean isReserve = true;

	/**
	 * @param runnable
	 * @param taskKey
	 * @param timeout
	 */
	public RunnableTask(TaskBuilder builder) {
		this.runnable = new ThreadRenamingRunnable(builder.runnable,
				builder.taskKey);
		this.taskKey = builder.taskKey;
		this.timeout = builder.timeout;
		this.taskId = LocalSeqIdGenerator.getGenerator().nextSeqId();
		if (builder.timeout > 0) {
			this.expireTimeout = SystemTimer.currentTimeMillis()
					+ builder.timeout;
		} else {
			this.expireTimeout = -1;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run() 线程在这里重新命名
	 */
	@Override
	public void run() {
		try {
			runnable.run();
		} finally {
		}
	}

	public boolean isExpire() {
		return (getTimeout() > 0)
				&& (expireTimeout < SystemTimer.currentTimeMillis());
	}

	public static class TaskBuilder {

		public static final String DEFAULT_TASK_KEY = "-DefaultTaskKey-";

		private final Runnable runnable;

		private String taskKey = DEFAULT_TASK_KEY;

		private long timeout = -1;

		public TaskBuilder(Runnable runnable) {
			this.runnable = runnable;
		}

		public TaskBuilder taskKey(String key) {
			this.taskKey = key;
			return this;
		}

		public TaskBuilder timeout(long timeout) {
			this.timeout = timeout;
			return this;
		}

		public RunnableTask build() {
			return new RunnableTask(this);
		}

		public static TaskBuilder newInstance(Runnable runnable) {
			return new TaskBuilder(runnable);
		}
	}

	public static class LocalSeqIdGenerator implements Iterable<Long> {

		private static final AtomicLong nextId = new AtomicLong(0);

		public static LocalSeqIdGenerator generator = new LocalSeqIdGenerator();

		private LocalSeqIdGenerator() {
		}

		@Override
		public Iterator<Long> iterator() {
			return new Iterator<Long>() {

				@Override
				public boolean hasNext() {
					return true;
				}

				@Override
				public Long next() {
					return nextId.incrementAndGet();
				}

				@Override
				public void remove() {
				}

			};
		}

		public long nextSeqId() {
			return iterator().next();
		}

		public static LocalSeqIdGenerator getGenerator() {
			return generator;
		}

	}

	public void onSuccess(MessageResult result) {

	}

	public void onFailed(MessageResult result) {

	}

	public long getExpireTimeout() {
		return expireTimeout;
	}

	public Runnable getRunnable() {
		return runnable;
	}

	public String getTaskKey() {
		return taskKey;
	}

	public long getTimeout() {
		return timeout;
	}

	public List<Quota> getCurrentUsedQuota() {
		return currentUsedQuota;
	}

	public void setCurrentUsedQuota(List<Quota> currentUsedQuota) {
		this.currentUsedQuota = currentUsedQuota;
	}

	public long getTaskId() {
		return taskId;
	}

	public boolean isReserve() {
		return isReserve;
	}

	public void setReserve(boolean isReserve) {
		this.isReserve = isReserve;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (taskId ^ (taskId >>> 32));
		result = prime * result + ((taskKey == null) ? 0 : taskKey.hashCode());
		result = prime * result + (int) (timeout ^ (timeout >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RunnableTask other = (RunnableTask) obj;
		if (taskId != other.taskId)
			return false;
		if (taskKey == null) {
			if (other.taskKey != null)
				return false;
		} else if (!taskKey.equals(other.taskKey))
			return false;
		if (timeout != other.timeout)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RunnableTask [currentUsedQuota=" + currentUsedQuota
				+ ", taskId=" + taskId + ", taskKey=" + taskKey + ", timeout="
				+ timeout + "]";
	}

}
