package org.googlecode.threadpool;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CentralExecutor implements Executor {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CentralExecutor.class);
	private static final String CLASS_NAME = CentralExecutor.class
			.getSimpleName();

	public final ExecutorService service;
	private final Policy policy;
	private final Map<Class<? extends Runnable>, Submitter> quotas;
	private final int threadSize;

	private int reserved;

	public CentralExecutor(final int threadSize, Policy policy) {
		this.threadSize = threadSize;
		this.policy = policy;
		this.service = Executors.newFixedThreadPool(threadSize,
				new NamedThreadFactory("jobDispatcher_worker"));
		this.quotas = new ConcurrentHashMap<Class<? extends Runnable>, Submitter>();
	}

	public CentralExecutor(int threadSize) {
		this(threadSize, Policy.PESSIMISM);
	}

	/** @see ExecutorService#shutdownNow() */
	public List<Runnable> shutdownNow() {
		return service.shutdownNow();
	}

	/** @see ExecutorService#shutdown() */
	public void shutdown() {
		service.shutdown();
	}

	/** @see ExecutorService#isShutdown() */
	public boolean isShutdown() {
		return service.isShutdown();
	}

	/** @see ExecutorService#isTerminated() */
	public boolean isTerminated() {
		return service.isTerminated();
	}

	/** @see ExecutorService#awaitTermination(long, java.util.concurrent.TimeUnit) */
	public boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException {
		return service.awaitTermination(timeout, unit);
	}

	@Override
	public void execute(Runnable task) {
		final Submitter submitter = quotas.get(task.getClass());
		if (submitter != null)
			submitter.submit(task, this);
		else
			policy.defaultSubmitter().submit(task, this);
	}

	/** @return 预留配额. */
	public static Quota reserve(int value) {
		return new Quota(value);
	}

	/** @return 弹性配额. */
	public static Quota elastic(int value) {
		return new Quota(value);
	}

	/** @return 零配额. */
	public static Quota nil() {
		return new Quota(0);
	}

	/**
	 * 设定taskClass的保留和限制配额.
	 * 
	 * @param taskClass
	 * @param reserve
	 * @param elastic
	 * 
	 * @throws IllegalArgumentException
	 */
	public void quota(Class<? extends Runnable> taskClass, Quota reserve,
			Quota elastic) {

		synchronized (this) {
			if (reserve.getValue() > threadSize - reserved)
				throw new IllegalArgumentException("No resource for reserve");
			reserved += reserve.getValue();
		}

		quotas.put(taskClass, policy.submitter(reserve, elastic));
	}

	public synchronized boolean hasUnreserved() {
		return threadSize > reserved;
	}
	public static class NamedThreadFactory implements ThreadFactory
	{
		static final AtomicInteger poolNumber = new AtomicInteger(1);

		final AtomicInteger threadNumber = new AtomicInteger(1);
		final ThreadGroup group;
		final String namePrefix;
		final boolean isDaemon;

		public NamedThreadFactory()
		{
			this("pool");
		}

		public NamedThreadFactory(String name)
		{
			this(name, false);
		}

		public NamedThreadFactory(String preffix, boolean daemon)
		{
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread()
					.getThreadGroup();
			namePrefix = preffix + "-" + poolNumber.getAndIncrement() + "-thread-";
			isDaemon = daemon;
		}

		public Thread newThread(Runnable r)
		{
			Thread t = new Thread(group, r, namePrefix
					+ threadNumber.getAndIncrement(), 0);
			t.setDaemon(isDaemon);
			if (t.getPriority() != Thread.NORM_PRIORITY)
			{
				t.setPriority(Thread.NORM_PRIORITY);
			}
			return t;

		}
	}
}
