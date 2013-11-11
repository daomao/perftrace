/**
 * 
 */
package org.googlecode.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 每一个不同的jobkey都会有默认的一套队列机制， 用于较为高效的处理队列中的数据
 * 
 * 
 */
public class TaskBufferQueue {

	private String taskKey;// 该类任务的主键
	private int maximumQueueSize;// 队列最大的长度
	private ReentrantLock lock;
	private Condition hasResource;// 是否有外部线程可用的信号量
	private AtomicInteger counter;// 队列计数器
	private QueueChecker checker;// 检查队列的后台线程
	private BlockingQueue<Task> jobQueue;// 内部队列

	public TaskBufferQueue(String taskKey, int maximumQueueSize,
			JobDispatcher jobDispatcher) {
		this.taskKey = taskKey;
		this.maximumQueueSize = maximumQueueSize;
		this.jobDispatcher = jobDispatcher;
		this.init();
	}

	public void init() {
		lock = new ReentrantLock();
		hasResource = lock.newCondition();
		counter = new AtomicInteger();
		jobQueue = new LinkedBlockingQueue<Job>(maximumQueueSize);

		checker = new QueueChecker();
		checker.setDaemon(true);
		checker.start();
	}

	public void clean() {
		counter.set(0);
		jobQueue.clear();
		checker.stopThread();
		checker = null;

	}

	public int size() {
		return counter.get();
	}

	/**
	 * 任务尝试入队列
	 * 
	 * @param job
	 * @return
	 */
	public boolean offer(Task t) {
		return jobQueue.offer(t);
	}

	/**
	 * 通知外部有资源可以执行任务
	 */
	public void notifyHasResource() {
		try {
			lock.lock();
			hasResource.signalAll();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 外部无可执行资源阻塞消息
	 * 
	 * @param waittime
	 */
	public void blockNoResource(long waittime) {
		boolean flag = lock.tryLock();
		if (flag) {
			try {
				hasResource.await(waittime, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ie) {
				// do nothing;
			} finally {
				lock.unlock();
			}
		}
	}

	class QueueChecker extends Thread {
		private boolean isRunning = true;

		public QueueChecker() {
			super("queueChecker-" + jobKey);
		}

		public void run() {

			while (isRunning) {

				try {
					// 尝试获取队列中任务，由于只有一个线程读取，因此可以采用peek + 判断后的poll
					Job job = jobQueue.peek();

					if (job != null) {
						// 判断是否有超时情况
						if (job.getTimeOut() > 0) {
							if (job.getTimeOut() < System.currentTimeMillis()) {
								jobQueue.poll();
								counter.decrementAndGet();
								continue;
							}
						}

						// 判断是否有资源，并且会先并发减去资源
						if (jobDispatcher.checkJobResource(job)) {
							// 如果有资源，弹出队列，执行任务，计数器递减
							jobQueue.poll();
							jobDispatcher.innExecute(job);
							counter.decrementAndGet();
						} else
							blockNoResource(5000);// 如果没有资源就阻塞5秒钟

					} else {
						blockNoJob(5000);// 如果没有任务就阻塞5秒钟
					}

				} catch (Exception ex) {
					log.error("QueueChecker error!", ex);
				}

			}

		}

		public void stopThread() {
			try {
				isRunning = false;
				this.interrupt();
			} catch (Exception e) {
				// do nothing
			}
		}
	}

}
