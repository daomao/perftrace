package org.googlecode.threadpool;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zhongfeng
 * 
 */
public class TimeoutChecker implements Runnable {

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
		this.exec = Executors.newSingleThreadExecutor(new NamedThreadFactory(
				THREAD_NAME_PREFIX + taskKey, false));
	}

	public void start() {
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
