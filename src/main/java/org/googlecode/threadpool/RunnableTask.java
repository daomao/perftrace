package org.googlecode.threadpool;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.googlecode.perftrace.stat.StatMonitor.SystemTimer;

/**
 * @author zhongfeng
 * 
 */
public class RunnableTask implements Runnable {

	private Runnable runnable;

	private String taskKey;

	private long timeout = -1;

	private long taskId;

	private long expireTimeout = -1;

	private volatile List<Quota> currentUsedQuota;

	private volatile boolean isReserve = true;

	public RunnableTask(Runnable runnable, String taskKey) {
		this(runnable, taskKey, -1, null);
	}
	/**
	 * @param runnable
	 * @param taskKey
	 * @param timeout
	 * 
	 */
	public RunnableTask(Runnable runnable, String taskKey, long timeout) {
		this(runnable,taskKey,timeout,null);
	}
	/**
	 * @param runnable
	 * @param taskKey
	 * @param timeout
	 * @param taskId
	 * @param currentUsedQuota
	 */
	public RunnableTask(Runnable runnable, String taskKey, long timeout,
			List<Quota> currentUsedQuota) {
		this.runnable = new ThreadRenamingRunnable(runnable, taskKey);
		this.taskKey = taskKey;
		this.timeout = timeout;
		this.taskId = LocalSeqIdGenerator.getGenerator().nextSeqId();
		this.currentUsedQuota = currentUsedQuota;
		if (timeout > 0) {
			this.expireTimeout = SystemTimer.currentTimeMillis() + timeout;
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
		return getTimeout() > 0
				&& (expireTimeout < SystemTimer.currentTimeMillis());
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

	public Runnable getRunnable() {
		return runnable;
	}

	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}

	public String getTaskKey() {
		return taskKey;
	}

	public void setTaskKey(String taskKey) {
		this.taskKey = taskKey;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
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

	public void setTaskId(long taskId) {
		this.taskId = taskId;
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
