package org.googlecode.threadpool;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** {@link Policy} */
public enum Policy {


	/** 乐观策略, 在存在为分配的配额情况下, 一旦出现闲置线程, 允许任务抢占, 抢占的优先级由提交的先后顺序决定. */
	OPTIMISM {

		/** 未定义配额的任务将直接进入等待队列, 但优先级低于所有定义了配额的任务. */
		public final Submitter defaultSubmitter = new Submitter() {
			@Override
			public void submit(Runnable task, CentralExecutor executor) {
				enqueue(new ComparableTask(task, Integer.MAX_VALUE));
			}
		};

		@Override
		public Submitter defaultSubmitter() {
			return defaultSubmitter;
		}

		@Override
		public Submitter submitter(final Quota reserve, final Quota elastic) {
			return new Submitter() {
				@Override
				public void submit(final Runnable task,
						CentralExecutor executor) {
					if (reserve.acquire())
						doSubmit(task, executor, reserve);
					// 若存在为分配的预留配额, 则弹性配额进行争抢
					else if (executor.hasUnreserved() && elastic.acquire())
						doSubmit(task, executor, elastic);
					// 同悲观策略进入等待队列
					else
						enqueue(new ComparableTask(task, reserve.getValue()));
				}
			};
		}
	},

	/** 悲观策略, 在所有线程都被预留的情况下, 即使当前预留之外的线程是空闲, 也不会被抢占, 即Elastic的设定将被忽略. */
	PESSIMISM {

		public final Submitter defaultSubmitter = new Submitter() {
			@Override
			public void submit(Runnable task, CentralExecutor executor) {
				throw new RejectedExecutionException(
						"Unquotaed task can not be executed in pessimism.");
			}
		};

		@Override
		public Submitter defaultSubmitter() {
			return defaultSubmitter;
		}

		@Override
		public Submitter submitter(final Quota reserve, final Quota elastic) {
			if (reserve.getValue() == 0)
				throw new IllegalArgumentException(
						"None-reserve task will never be executed in pessimism.");

			return new Submitter() {
				@Override
				public void submit(final Runnable task,
						CentralExecutor executor) {
					if (reserve.acquire())
						doSubmit(task, executor, reserve);
					// 耗尽预留配额后, 进入等待队列, 按预留额度大小排优先级, 大者优先.
					else
						enqueue(new ComparableTask(task, reserve.getValue()));
				}
			};
		}
	};

	/** 优先级等待队列. */
	private final PriorityBlockingQueue<ComparableTask> queue = new PriorityBlockingQueue<ComparableTask>();

	public abstract Submitter submitter(Quota reserve, Quota elastic);

	public abstract Submitter defaultSubmitter();
	
	private static final Logger LOGGER = LoggerFactory
	.getLogger(CentralExecutor.class);
	/** 将任务入等待队列. */
	void enqueue(ComparableTask task) {
		queue.put(task);
		LOGGER.debug("Enqueue {}", task.original);
	}

	/** 将任务出列重新提交给执行器. */
	void dequeueTo(CentralExecutor executor) {
		try {
			final Runnable task = queue.take().original;
			LOGGER.debug("Dequeue {}", task);
			executor.execute(task);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.debug("Dequeue has been interrupted ", e);
		}
	}

	void doSubmit(Runnable task, CentralExecutor executor, Quota quota) {
		executor.service.execute(new Decorator(task, quota, executor));
	}

	/** {@link ComparableTask} */
	static class ComparableTask implements Comparable<ComparableTask> {
		final Runnable original;
		private final int quota;

		public ComparableTask(Runnable task, int quota) {
			this.original = task;
			this.quota = quota;
		}

		@Override
		public int compareTo(ComparableTask o) {
			return -(quota - o.quota);
		}
	}

	/** {@link Decorator} */
	class Decorator implements Runnable {
		private final Runnable task;
		private final Quota quota;
		private final CentralExecutor executor;

		public Decorator(Runnable task, Quota quota,
				CentralExecutor executor) {
			this.task = task;
			this.quota = quota;
			this.executor = executor;
		}

		@Override
		public void run() {
			try {
				task.run();
			} catch (Throwable t) {
				LOGGER.error("Unexpected Interruption cause by", t);
			} finally {
				quota.release();
				dequeueTo(executor);
			}
		}
	}
}	/** {@link Policy} */
