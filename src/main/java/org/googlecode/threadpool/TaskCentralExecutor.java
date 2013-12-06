package org.googlecode.threadpool;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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

import org.googlecode.threadpool.util.NamedThreadFactory;
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

	private final TimeoutMonitor timeoutMonitor = TimeoutMonitor.getInstance();;

	private final LoadingCache<String, BufferQueue> bufferQueueRepo;

	/**
	 * 
	 */
	private final CopyOnWriteArrayList<BufferQueue> bufferQueueList;

	private final TaskQuotaAllocator taskQuotaAllocator;

	private final PoolConfig poolConfig;

	private static TaskCentralExecutor INSTANCE;

	private final ExecutorService exec = Executors
			.newSingleThreadExecutor(new NamedThreadFactory(
					"Task-Notify-Resource-Thread", false));

	private TaskCentralExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, PoolConfig poolConfig) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		this.poolConfig = poolConfig;
		this.taskQuotaAllocator = TaskQuotaAllocator.getInstance(poolConfig);
		this.bufferQueueList = new CopyOnWriteArrayList<BufferQueue>();
		this.bufferQueueRepo = initBufferQueueCache(poolConfig,
				this.bufferQueueList);
	}

	private LoadingCache<String, BufferQueue> initBufferQueueCache(
			PoolConfig poolConfig,
			CopyOnWriteArrayList<BufferQueue> bufferQueueList) {
		return CacheBuilder.newBuilder().build(
				new BufferQueueCacheLoader(poolConfig, bufferQueueList,this));
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		RunnableTask task = null;
		if (r instanceof RunnableTask) {
			task = (RunnableTask) r;
		} else if (r instanceof FutureTaskDelay) {
			task = ((FutureTaskDelay) r).getTask();
		}
		if (task != null) {
			LOG.debug("CentralExecutor-AfterExecute release:{}", task);
			releaseTask(task);
		} else {
			LOG.warn("Error. r is : {}", r);
		}
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		LOG.debug("CentralExecutor-BeforeExecute", r);
	}

	public void runTask(RunnableTask runnableTask) {
		if (runnableTask.getTimeout() > 0) {
			FutureTaskDelay fTask = timeoutMonitor
					.addTaskTimeoutMonitor(runnableTask);
			LOG.debug("Run task : {} at : {}", runnableTask, new Date());
			execute(fTask);
		} else {
			LOG.debug("Run task : {} at : {}", runnableTask, new Date());
			execute(runnableTask);
		}
	}

	/**
	 * @param runnableTask
	 * @return
	 */
	public boolean acquireResource(RunnableTask runnableTask) {
		return taskQuotaAllocator.acquire(runnableTask);
	}

	/**
	 * @param runnableTask
	 * @return
	 */
	public void releaseResource(RunnableTask runnableTask) {
		taskQuotaAllocator.release(runnableTask);
	}

	/**
	 * 提交任务，存在一个优先级问题 如果队列不为空，提交执行的任务会被放入队列中
	 */
	public void submitTask(RunnableTask runnableTask) {
		LOG.debug("Submit task : {} , at : {}", runnableTask, new Date());
		if (hasWaitTask(runnableTask)) {
			LOG.debug("HasWaitTask.Put in Buffer Q. {}", runnableTask);
			putQueue(runnableTask);
		} else {
			if (acquireResource(runnableTask)) {
				runTask(runnableTask);
			} else {
				LOG.debug("NoResource. Put in Buffer Q. {}", runnableTask);
				putQueue(runnableTask);
			}
		}
	}

	/**
	 * @param runnableTask
	 */
	private void releaseTask(RunnableTask runnableTask) {
		LOG.debug("Release task : {}", runnableTask);
		// 已完成的Task，取消超时监控
		timeoutMonitor.cancelTaskTimeoutMonitor(runnableTask);
		// 资源释放,计数器增加
		releaseResource(runnableTask);
		// 通知该服务有可用资源
		notifyHasResource(runnableTask);
	}

	public synchronized static TaskCentralExecutor getInstance(
			PoolConfig poolConfig) {
		if (INSTANCE == null) {
			INSTANCE = new TaskCentralExecutor(0, Integer.MAX_VALUE, 60L,
					TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
					poolConfig);
		}
		return INSTANCE;
	}

	public boolean putQueue(RunnableTask task) {
		return getTaskQueue(task).addTask(task);
	}

	public boolean hasWaitTask(RunnableTask task) {
		return (!getTaskQueue(task).isEmpty());
	}

	public void notifyHasResource(final RunnableTask task) {
		getTaskQueue(task).notifyHasResource();
		if (!task.isReserve()) {
			exec.submit(new Runnable() {
				@Override
				public void run() {
					// 需要改进，有一点性能问题
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

	public TimeoutMonitor getTimeoutMonitor() {
		return timeoutMonitor;
	}

	public CopyOnWriteArrayList<BufferQueue> getBufferQueueList() {
		return bufferQueueList;
	}

	public TaskQuotaAllocator getTaskQuotaAllocator() {
		return taskQuotaAllocator;
	}

	public PoolConfig getPoolConfig() {
		return poolConfig;
	}

	public void shutdown() {
		Iterator<Entry<String, BufferQueue>> iter = getBufferQueueRepo()
				.asMap().entrySet().iterator();
		while (iter.hasNext()) {
			iter.next().getValue().stop();
		}
		getBufferQueueRepo().invalidateAll();
	}

	public final static class BufferQueueCacheLoader extends
			CacheLoader<String, BufferQueue> {
		private PoolConfig poolConfig;
		private CopyOnWriteArrayList<BufferQueue> bufferQueueList;
		private TaskCentralExecutor centralExecutor;

		/**
		 * @param poolConfig
		 */
		public BufferQueueCacheLoader(PoolConfig poolConfig,
				CopyOnWriteArrayList<BufferQueue> bufferQueueList,
				TaskCentralExecutor centralExecutor) {
			this.poolConfig = poolConfig;
			this.bufferQueueList = bufferQueueList;
			this.centralExecutor = centralExecutor;
		}

		@Override
		public BufferQueue load(String key) throws Exception {
			BufferQueue queue = BufferQueue.newInstance(key, poolConfig
					.getTaskConfig(key).getBufferSize());
			bufferQueueList.add(queue);
			queue.startQueueTaskSubmit(centralExecutor);
			return queue;
		}
	}
}
