package org.googlecode.threadpool;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * @author zhongfeng
 * 
 */
public class TaskCentralExecutor extends ThreadPoolExecutor {
	
	private static final Logger LOG = LoggerFactory
	.getLogger(TaskCentralExecutor.class);
	
	private TimeoutMonitor timeoutMonitor = TimeoutMonitor.getInstance();;

	private LoadingCache<String, BufferQueue> bufferQueueRepo;
	
	private final CopyOnWriteArrayList<BufferQueue> bufferQueueList = new CopyOnWriteArrayList<BufferQueue>();

	private final static TaskCentralExecutor BUSSINESS_POOL = TaskCentralExecutor
			.newTaskCentralExecutor();

	private TaskQuotaAllocator taskQuotaAllocator = TaskQuotaAllocator
			.getInstance();

	private final ExecutorService exec = Executors
			.newSingleThreadExecutor(new NamedThreadFactory(
					"Task-Notify-Resource-Thread", false));

	private TaskCentralExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		this.bufferQueueRepo = initBufferQueueCache();
	}

	private LoadingCache<String, BufferQueue> initBufferQueueCache() {
		CacheLoader<String, BufferQueue> runStatsLoader = new CacheLoader<String, BufferQueue>() {
			@Override
			public BufferQueue load(String key) throws Exception {
				BufferQueue queue = BufferQueue.newInstance(key, PoolConfig
						.getTaskConfig(key).getBufferSize());
				bufferQueueList.add(queue);
				queue.startQueueTaskSubmit(BUSSINESS_POOL, taskQuotaAllocator);
				return queue;
			}
		};
		return CacheBuilder.newBuilder().build(runStatsLoader);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		if (r instanceof RunnableTask) {
			LOG.debug("CentralExecutor-AfterExecute release:{}",r);
			releaseTask((RunnableTask) r);
		}
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		if (r instanceof RunnableTask) {
		}
	}

	public void runTask(RunnableTask runnableTask) {
		if (runnableTask.getTimeout() > 0) {
			FutureTaskDelay fTask = timeoutMonitor
					.addTaskTimeoutMonitor(runnableTask);
			execute(fTask);
		} else {
			execute(runnableTask);
		}
	}

	/**
	 * @param runnableTask
	 * @return
	 */
	public boolean checkJobResource(RunnableTask runnableTask) {
		return taskQuotaAllocator.acquire(runnableTask);
	}

	/**
	 * 提交任务，存在一个优先级问题 如果队列不为空，提交执行的任务会被放入队列中
	 */
	public void submitTask(RunnableTask runnableTask) {
		if (hasWaitTask(runnableTask)) {
			putQueue(runnableTask);
		} else {
			if (checkJobResource(runnableTask)) {
				runTask(runnableTask);
			} else {
				putQueue(runnableTask);
			}
		}
	}

	/**
	 * @param runnableTask
	 */
	private void releaseTask(RunnableTask runnableTask) {
		// 已完成的Task，取消超时监控
		timeoutMonitor.cancelTaskTimeoutMonitor(runnableTask);
		// 资源释放,计数器增加
		taskQuotaAllocator.release(runnableTask);
		// 通知该服务有可用资源
		notifyHasResource(runnableTask);
	}

	public final static TaskCentralExecutor newTaskCentralExecutor() {
		return new TaskCentralExecutor(0, Integer.MAX_VALUE, 60L,
				TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	}

	public boolean putQueue(RunnableTask task) {
		return getTaskQueue(task).addTask(task);
	}

	public boolean hasWaitTask(RunnableTask task) {
		return getTaskQueue(task).isEmpty();
	}

	public void notifyHasResource(final RunnableTask task) {
		getTaskQueue(task).notifyHasResource();
		if (!task.isReserve()) {
			exec.submit(new Runnable() {
				@Override
				public void run() {
					//需要改进，有一点性能问题
					Collections.shuffle(bufferQueueList);
					for (BufferQueue taskQueue : bufferQueueList)
						taskQueue.notifyHasResource();
				}
			});
		}
	}

	/**
	 * @param task
	 */
	private BufferQueue getTaskQueue(RunnableTask task) {
		BufferQueue queue = null;
		try {
			queue = getBufferQueueRepo().get(task.getTaskKey());
		} catch (ExecutionException e) {
			// LOG.error("Get Queue Error", e);
		}
		return queue;
	}

	public LoadingCache<String, BufferQueue> getBufferQueueRepo() {
		return bufferQueueRepo;
	}

	public Collection<BufferQueue> getAllBufferQueue() {
		return getBufferQueueRepo().asMap().values();
	}

	public void shutdown() {
		Iterator<Entry<String, BufferQueue>> iter = getBufferQueueRepo()
				.asMap().entrySet().iterator();
		while (iter.hasNext()) {
			iter.next().getValue().stop();
		}
		getBufferQueueRepo().invalidateAll();
	}
}
