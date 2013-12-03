/**
 * 
 */
package org.googlecode.threadpool;

import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * @author zhongfeng
 * 
 */
public class FutureTaskDelay extends FutureTask<Object> implements Delayed {

	private final RunnableTask task;

	private final long timeout;

	public FutureTaskDelay(RunnableTask task, Object result) {
		super(task, result);
		this.task = task;
		this.timeout = TimeUnit.NANOSECONDS.convert(task.getTimeout(),
				TimeUnit.MILLISECONDS)
				+ System.nanoTime();
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(timeout - System.nanoTime(), TimeUnit.NANOSECONDS);
	}

	@Override
	public int compareTo(Delayed o) {
		FutureTaskDelay that = (FutureTaskDelay) o;
		return timeout > that.getTimeout() ? 1
				: (timeout < that.getTimeout() ? -1 : 0);
	}

	public RunnableTask getTask() {
		return task;
	}

	public long getTimeout() {
		return timeout;
	}

	public static FutureTaskDelay newInstance(RunnableTask task) {
		return new FutureTaskDelay(task, null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((task == null) ? 0 : task.hashCode());
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
		FutureTaskDelay other = (FutureTaskDelay) obj;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (!task.equals(other.task))
			return false;
		return true;
	}
}
