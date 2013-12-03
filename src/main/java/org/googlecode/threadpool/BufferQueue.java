/**
 * 
 */
package org.googlecode.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.googlecode.perftrace.stat.StatMonitor.SystemTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task 没有资源
 * @author zhongfeng
 * 
 */
public class BufferQueue {

	private static final Logger LOG = LoggerFactory
			.getLogger(BufferQueue.class);

	/**
	 * 默认缓存对列大小
	 */
	private static final int DEFAULT_QUEUE_SIZE = 500;
	
	/**
	 * 缓存任务提交线程命名前缀
	 */
	private static final String BUFFER_QUEUE_THREAD_PREFIEX = "BufferQueueThread-";

	/**
	 * 任务主键
	 */
	private final String taskKey;

	/**
	 * 缓存队列大小
	 */
	private final int maximumQueueSize;

	/**
	 * 内部队列
	 */
	private BlockingQueue<RunnableTask> taskBufferQueue;

	/**
	 * 缓存任务提交线程
	 */
	private ExecutorService exec;

	/**
	 * 
	 */
	private ReentrantLock lock = new ReentrantLock();

	/**
	 * 有资源释放的Condition
	 */
	private Condition hasResource = lock.newCondition();

	/**
	 * 有新任务加入的Condition
	 */
	private Condition hasJob = lock.newCondition();

	/**
	 * @param taskKey
	 * @param maximumQueueSize
	 */
	private BufferQueue(String taskKey, int maximumQueueSize) {
		this.taskKey = taskKey;
		this.maximumQueueSize = maximumQueueSize;
		this.taskBufferQueue = new LinkedBlockingQueue<RunnableTask>(
				maximumQueueSize);
		this.exec = Executors.newSingleThreadExecutor(new NamedThreadFactory(
				BUFFER_QUEUE_THREAD_PREFIEX + taskKey, false));
	}

	/**
	 * 开启缓存任务提交，在queue创建完毕后开启
	 * 
	 * @param centralExecutor 核心业务主线程池
	 * @param taskQuotaAllocator 配额资源管理器
	 */
	public void startQueueTaskSubmit(TaskCentralExecutor centralExecutor,
			TaskQuotaAllocator taskQuotaAllocator) {
		exec
				.submit(new BufferTaskSubmitter(centralExecutor,
						taskQuotaAllocator));
	}

	public void stop() {
		taskBufferQueue.clear();
		try {
			exec.awaitTermination(5000L, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		exec.shutdownNow();

	}

	/**
	 * 任务尝试入队列
	 * 
	 * @param job
	 * @return
	 */
	public boolean addTask(RunnableTask task) {
		LOG.debug("AddTask：Q：{}, Task: {}",getTaskKey(),task);
		boolean result = taskBufferQueue.offer(task);
		if (result) {
			notifyHasJob();
		}
		return result;
	}

	public boolean isEmpty() {
		return taskBufferQueue.isEmpty();
	}

	/**
	 * 通知有任务已经入队列
	 */
	private void notifyHasJob() {
		lock.lock();
		try {
			hasJob.signalAll();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 通知外部有资源可以执行任务
	 */
	public void notifyHasResource() {
		boolean flag = lock.tryLock();
		try {
			if (flag)
				hasResource.signalAll();
		} finally {
			if (flag)
				lock.unlock();
		}
	}

	/**
	 * 队列中无任务的阻塞消息
	 * 
	 * @param waittime
	 */
	private void blockNoJob(long waittime) {
		lock.lock();
		try {
			hasJob.await(waittime, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ie) {
			// do nothing;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 外部无可执行资源阻塞消息
	 * 
	 * @param waittime
	 */
	private void blockNoResource(long waittime) {
		lock.lock();
		try {
			hasResource.await(waittime, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ie) {
			// do nothing;
		} finally {
			lock.unlock();
		}
	}

	private class BufferTaskSubmitter implements Runnable {

		private TaskCentralExecutor centralExecutor;

		private TaskQuotaAllocator taskQuotaAllocator;

		public BufferTaskSubmitter(TaskCentralExecutor centralExecutor,
				TaskQuotaAllocator taskQuotaAllocator) {
			this.centralExecutor = centralExecutor;
			this.taskQuotaAllocator = taskQuotaAllocator;
		}

		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				if (taskBufferQueue.isEmpty()) {
					blockNoJob(1000);// 如果没有任务就阻塞1秒钟
					continue;
				}
				RunnableTask task = taskBufferQueue.peek();
				// 判断是否有超时情况
				if (task.getTimeout() > 0
						&& (task.getTimeout() < SystemTimer.currentTimeMillis())) {
					taskBufferQueue.poll();
					continue;
				}
				// 判断是否有资源，并且会先并发减去资源
				if (taskQuotaAllocator.acquire(task)) {
					// 如果有资源，弹出队列，执行任务，计数器递减
					taskBufferQueue.poll();
					centralExecutor.runTask(task);
				} else {
					LOG.debug("BufferQueue NO Resource: {}",getTaskKey());
					blockNoResource(1000);// 如果没有资源就阻塞1秒钟
				}
			}// while
		}// run
	}

	public static BufferQueue newInstance(String taskKey, int maximumQueueSize) {
		return new BufferQueue(taskKey, maximumQueueSize);
	}

	public static BufferQueue newInstance(String taskKey) {
		return newInstance(taskKey, DEFAULT_QUEUE_SIZE);
	}

	public String getTaskKey() {
		return taskKey;
	}

	public int getMaximumQueueSize() {
		return maximumQueueSize;
	}

	public BlockingQueue<RunnableTask> getTaskBufferQueue() {
		return taskBufferQueue;
	}
}
