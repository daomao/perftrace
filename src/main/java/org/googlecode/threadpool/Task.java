package org.googlecode.threadpool;

import java.util.List;

/**
 * @author zhongfeng
 *
 */
public abstract class Task implements Runnable{
	
	private Runnable runnable;
	
	private String taskKey;
	
	private long timeout;
	
	private volatile List<Quota> currentUsedQuota;

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

	@Override
	public void run() {
			try{
				runnable.run();
			}
			finally
			{
				
			}
	}
}
