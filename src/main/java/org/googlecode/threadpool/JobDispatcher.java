package org.googlecode.threadpool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.googlecode.threadpool.CentralExecutor.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 简单的任务分发类，支持根据业务资源分配模型来分配线程资源，当前支持两类 1. 根据某一类key可以预留多少资源独享。 2.
 * 根据某一类key可以限制最多使用多少资源。
 * 
 * @author fangweng
 */
public class JobDispatcher implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(JobDispatcher.class);

	private TaskCentralExecutor threadPool;// 内部线程池
	private JobThreadWeightModel[] jobThreadWeightModel;// 内置资源分配线程池模型，可运行期动态构建
	private Map<String, AtomicInteger> counterPool;// 记录每一个设置了资源分配模型的资源所占用的私有线程数（如果是limit就和defaultCounterPool保持一致）
	private Map<String, TaskBufferQueue> jobQueuePool;// 不同的资源分配模型配置key都有自己的队列结构，默认的模型采用default。
	private Map<String, AtomicInteger> defaultCounterPool;// 用于记录每一个设置了资源分配模型的资源所占用的真实线程数。（包括私有和共有的）

	private TaskQuotaFactory quotaFactory;// 任务阀值

	private AtomicInteger defaultCounter;// 默认线程消耗数量，totalcounter-各个私有线程消耗
	private AtomicInteger totalCounter;// 所有线程消耗数量
	private int maximumPoolSize = 200;
	private int corePoolSize = 50;
	private int maximumQueueSize = 1000;
	private int maxQueueDispatcherSize = 2;

	private PriorityBlockingQueue<JobFutureTask> futureTaskTTLList;// 用于check超时执行的线程，尝试取消

	/**
	 * 保留型的资源配置，优先使用保留的资源
	 */
	private boolean privateUseFirst = true;

	/**
	 * 任务失效时间，全局统一，单位秒
	 */
	private int jobTimeOut = 0;
	private Thread timeoutChecker;
	private boolean runFlag = true;
	private ReentrantLock checkerLock;
	private Condition hasJobCondition;
	private boolean timeOutAfterExecute = false;// 是否从执行开始设置timeout时间

	public boolean isTimeOutAfterExecute() {
		return timeOutAfterExecute;
	}

	public void setTimeOutAfterExecute(boolean timeOutAfterExecute) {
		this.timeOutAfterExecute = timeOutAfterExecute;
	}

	public PriorityBlockingQueue<JobFutureTask> getFutureTaskTTLList() {
		return futureTaskTTLList;
	}

	public void setFutureTaskTTLList(
			PriorityBlockingQueue<JobFutureTask> futureTaskTTLList) {
		this.futureTaskTTLList = futureTaskTTLList;
	}

	public Map<String, TaskBufferQueue> getJobQueuePool() {
		return jobQueuePool;
	}

	public int getMaxQueueDispatcherSize() {
		return maxQueueDispatcherSize;
	}

	public int getJobTimeOut() {
		return jobTimeOut;
	}

	public void setJobTimeOut(int jobTimeOut) {
		this.jobTimeOut = jobTimeOut;
	}

	public void setMaxQueueDispatcherSize(int maxQueueDispatcherSize) {
		this.maxQueueDispatcherSize = maxQueueDispatcherSize;
	}

	public JobThreshold getJobThreshold() {
		return jobThreshold;
	}

	public void setJobThreshold(JobThreshold jobThreshold) {
		this.jobThreshold = jobThreshold;
	}

	public boolean isPrivateUseFirst() {
		return privateUseFirst;
	}

	public void setPrivateUseFirst(boolean privateUseFirst) {
		this.privateUseFirst = privateUseFirst;
	}

	public int getDefaultQueueCounter() {
		return jobQueuePool.get(DEFAULT_JOBQUEUE).size();
	}

	public TaskCentralExecutor getThreadPool() {
		return threadPool;
	}

	public void setThreadPool(TaskCentralExecutor threadPool) {
		this.threadPool = threadPool;
	}

	public void init() {
		if (threadPool == null)
			threadPool = new TaskCentralExecutor(corePoolSize,
					maximumPoolSize, 0L, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>(maximumQueueSize),
					new NamedThreadFactory("jobDispatcher_worker"), this);

		defaultCounter = new AtomicInteger(0);
		totalCounter = new AtomicInteger(0);
		counterPool = new ConcurrentHashMap<String, AtomicInteger>();
		defaultCounterPool = new ConcurrentHashMap<String, AtomicInteger>();
		jobQueuePool = new ConcurrentHashMap<String, TaskBufferQueue>();

		futureTaskTTLList = new PriorityBlockingQueue<JobFutureTask>();

		timeoutChecker = new Thread(this, "jobDispatcher-TimeoutChecker");
		timeoutChecker.setDaemon(true);
		timeoutChecker.start();

		checkerLock = new ReentrantLock();
		hasJobCondition = checkerLock.newCondition();

		// 默认无key的任务队列
		TaskBufferQueue q = new TaskBufferQueue(DEFAULT_JOBQUEUE, maximumQueueSize, this);
		jobQueuePool.put(DEFAULT_JOBQUEUE, q);

		jobThreshold = buildWeightModel(jobThreadWeightModel);

	}

	/**
	 * 运行期修改模型
	 * 
	 * @param newJobThreadWeightModel
	 */
	public JobThreshold buildWeightModel(
			JobThreadWeightModel[] newJobThreadWeightModel) {
		this.jobThreadWeightModel = newJobThreadWeightModel;
		JobThreshold newJobThreshold = new JobThreshold();
		newJobThreshold.setDefaultThreshold(maximumPoolSize);

		if (newJobThreadWeightModel != null
				&& newJobThreadWeightModel.length > 0) {
			for (JobThreadWeightModel j : newJobThreadWeightModel) {
				try {
					// 构建设置资源分配模型所需要的附属对象
					if (counterPool.get(j.getKey()) == null) {
						counterPool.put(j.getKey(), new AtomicInteger(0));

						TaskBufferQueue q = new TaskBufferQueue(j.getKey(), maximumQueueSize,
								this);
						jobQueuePool.put(j.getKey(), q);

						defaultCounterPool
								.put(j.getKey(), new AtomicInteger(0));
					}

					if (j.getValue() == 0)
						continue;

					if (j.getType().equals(
							JobThreadWeightModel.WEIGHT_MODEL_LIMIT)) {
						newJobThreshold.getThresholdPool().put(j.getKey(),
								j.getValue());
					} else if (j.getType().equals(
							JobThreadWeightModel.WEIGHT_MODEL_LEAVE)) {
						newJobThreshold.getThresholdPool().put(j.getKey(),
								j.getValue());
						newJobThreshold.setDefaultThreshold(newJobThreshold
								.getDefaultThreshold()
								- j.getValue());
					} else {
						log.error(new StringBuilder(
								"thread weight config type:").append(
								j.getType()).append(" key:").append(j.getKey())
								.append(" value:").append(j.getValue()).append(
										" not support!"));
					}
				} catch (Exception ex) {
					log.error("create jobWeightModels: " + j.getKey()
							+ " error!", ex);
				}
			}

			if (newJobThreshold.getDefaultThreshold() <= 0)
				throw new RuntimeException(
						"total leave resource > total resource...");

			for (JobThreadWeightModel j : newJobThreadWeightModel) {
				try {
					if (j.getValue() == 0)
						continue;

					if (j.getType().equals(
							JobThreadWeightModel.WEIGHT_MODEL_LIMIT)) {
						if (newJobThreshold.getThresholdPool().get(j.getKey()) > newJobThreshold
								.getDefaultThreshold())
							newJobThreshold.getThresholdPool().put(j.getKey(),
									-newJobThreshold.getDefaultThreshold());
						else
							newJobThreshold.getThresholdPool().put(
									j.getKey(),
									-newJobThreshold.getThresholdPool().get(
											j.getKey()));
					}
				} catch (Exception ex) {
					log.error("create jobWeightModels: " + j.getKey()
							+ " error!", ex);
				}
			}
		}

		return newJobThreshold;
	}

	/**
	 * 兼容普通的runnable的提交
	 * 
	 * @param job
	 */
	public void execute(Runnable job) {
		if (job instanceof Task)
			execute((Task) job);
		else
			threadPool.execute(job);
	}

	/**
	 * 检查是否有资源可用， 注意检查过程已经有对资源数值作修改的动作，不可重复调用，避免资源泄露
	 * 
	 * @param task
	 * @return
	 */
	public boolean checkJobResource(Task task) {
		boolean hasResource = false;

		// 第一层做总量判断，同时锁定总资源
		if (totalCounter.incrementAndGet() > this.maximumPoolSize) {
			totalCounter.decrementAndGet();
			return false;
		}

		String key = task.getKey();
		Integer threshold = null;
		if (key != null)
			threshold = jobThreshold.getThresholdPool().get(key);

		if (key == null || threshold == null) {
			// 使用默认资源，计数器累加比较判断是否有资源
			if (defaultCounter.incrementAndGet() > jobThreshold
					.getDefaultThreshold()) {
				defaultCounter.decrementAndGet();
			} else {
				hasResource = true;
			}
		} else {
			AtomicInteger counter = counterPool.get(key);
			if (threshold > 0) {// leave mode
				// leave模式下，可以选择先用私有的资源
				if (privateUseFirst) {
					if (counter.incrementAndGet() > threshold) {
						counter.decrementAndGet();

						// 私有的用完了话，考虑用共有的
						if (defaultCounter.incrementAndGet() > jobThreshold
								.getDefaultThreshold()) {
							defaultCounter.decrementAndGet();
						} else {
							hasResource = true;
						}
					} else {
						hasResource = true;
					}
				} else {
					// 先用公有的，如果没有资源在判断是否有私有的
					if (defaultCounter.incrementAndGet() > jobThreshold
							.getDefaultThreshold()) {
						defaultCounter.decrementAndGet();
						if (counter.incrementAndGet() > threshold)
							counter.decrementAndGet();
						else
							hasResource = true;
					} else {
						hasResource = true;
					}
				}

			} else {// limit模式下，检查是否超过了阀值，limit得阀值设置为负数用于和leave区分
				if (counter.incrementAndGet() > -threshold) {
					counter.decrementAndGet();
				} else {
					if (defaultCounter.incrementAndGet() > jobThreshold
							.getDefaultThreshold()) {
						defaultCounter.decrementAndGet();
						counter.decrementAndGet();
					} else {
						hasResource = true;
					}
				}
			}
		}

		if (!hasResource)
			totalCounter.decrementAndGet();

		return hasResource;
	}

	/**
	 * 提交任务
	 */
	public void execute(Task task) {

		boolean hasResource = checkJobResource(task);

		// 注意，如果设置了jobtimeout，那么job自身进入时如果已经设置timeout将会被覆盖
		if (jobTimeOut > 0 && !timeOutAfterExecute) {
			task.setTimeOut(System.currentTimeMillis() + jobTimeOut * 1000);
		}

		if (hasResource) {
			// 注意，如果设置了jobtimeout，那么job自身进入时如果已经设置timeout将会被覆盖
			if (jobTimeOut > 0 && timeOutAfterExecute) {
				task.setTimeOut(System.currentTimeMillis() + jobTimeOut * 1000);
			}

			innExecute(task);
		} else {
			pushJob(task);
		}
	}

	public JobFutureTask innExecute(Task task) {
		JobFutureTask ftask = null;

		if (task.getTimeOut() > 0) {
			ftask = new JobFutureTask(task, null);
			threadPool.execute(ftask);
		} else
			threadPool.execute(task);

		return ftask;
	}

	public JobThreadWeightModel[] getJobThreadWeightModel() {
		return jobThreadWeightModel;
	}

	public void setJobThreadWeightModel(
			JobThreadWeightModel[] jobThreadWeightModel) {
		this.jobThreadWeightModel = jobThreadWeightModel;
	}

	public void setJobThreadWeightModel(
			List<JobThreadWeightModel> jobThreadWeightModel) {
		this.jobThreadWeightModel = jobThreadWeightModel
				.toArray(new JobThreadWeightModel[0]);
	}

	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public void setMaximumPoolSize(int maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}

	public int getMaximumQueueSize() {
		return this.maximumQueueSize;
	}

	public void setMaximumQueueSize(int maximumQueueSize) {
		this.maximumQueueSize = maximumQueueSize;
	}

	public Map<String, AtomicInteger> getCounterPool() {
		return counterPool;
	}

	public Map<String, Integer> getThresholdPool() {
		return jobThreshold.getThresholdPool();
	}

	public int getDefaultThreshold() {
		return jobThreshold.getDefaultThreshold();
	}

	public AtomicInteger getDefaultCounter() {
		return defaultCounter;
	}

	public AtomicInteger getTotalCounter() {
		return totalCounter;
	}

	public Map<String, AtomicInteger> getDefaultCounterPool() {
		return defaultCounterPool;
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	/**
	 * 任务入队列
	 * 
	 * @param task
	 */
	public void pushJob(Task task) {
		TaskBufferQueue taskBufferQueue = getJobQueue(task);

		if (!taskBufferQueue.offer(task)) {// 补偿job
			throw new RuntimeException("can't submit job, queue full...");
		}

	}

	/**
	 * 根据job类型获取对应的队列
	 * 
	 * @param task
	 * @return
	 */
	public TaskBufferQueue getJobQueue(Task task) {
		TaskBufferQueue taskBufferQueue;

		// 采用默认的
		if (task.getKey() == null
				|| (task.getKey() != null && !jobQueuePool.containsKey(task
						.getKey()))) {
			taskBufferQueue = jobQueuePool.get(DEFAULT_JOBQUEUE);
		} else {
			taskBufferQueue = jobQueuePool.get(task.getKey());
		}

		return taskBufferQueue;
	}

	/**
	 * 线程执行前的操作
	 * 
	 * @param task
	 */
	public void beforeExecuteJob(Task task) {
		// 用于统计默认线程中不同的请求消耗的线程数
		if (task.getKey() != null
				&& defaultCounterPool.containsKey(task.getKey())) {
			defaultCounterPool.get(task.getKey()).incrementAndGet();
		}

		// 用于超时检查
		if (task instanceof JobFutureTask) {
			futureTaskTTLList.add((JobFutureTask) task);
			notifyHasTimeOutJob();
		}

	}

	/**
	 * 释放线程时对于各种计数器做递减
	 * 
	 * @param task
	 */
	public void releaseJob(Task task) {

		// 用于删除
		if (task instanceof JobFutureTask) {
			futureTaskTTLList.remove((JobFutureTask) task);
		}

		// 需要增加notify的代码
		String key = task.getKey();

		this.getTotalCounter().decrementAndGet();

		if (this.getCounterPool().size() == 0 || key == null) {
			this.getDefaultCounter().decrementAndGet();
		} else {
			AtomicInteger counter = this.getCounterPool().get(key);

			if (counter != null) {

				if (defaultCounterPool.get(key) != null) {
					defaultCounterPool.get(key).decrementAndGet();
				}

				// leave先还私有的
				if (counter.decrementAndGet() < 0) {
					counter.incrementAndGet();
					this.getDefaultCounter().decrementAndGet();
				} else {
					Integer size = this.getThresholdPool().get(key);

					if (size == null || (size != null && size < 0)) { // limit
						// mode
						// (use
						// default)
						// counter.decrementAndGet();
						this.getDefaultCounter().decrementAndGet();
					} else { // leave mode (use itself)
						// nothing to do
					}
				}
			} else {
				this.getDefaultCounter().decrementAndGet();
			}
		}

		// 释放资源信号,必须放在最后
		TaskBufferQueue taskBufferQueue = getJobQueue(task);
		taskBufferQueue.notifyHasResource();

	}

	/**
	 * 通知有任务已经入队列
	 */
	public void notifyHasTimeOutJob() {
		boolean flag = false;

		try {
			flag = checkerLock.tryLock(50, TimeUnit.MILLISECONDS);

			if (flag)
				hasJobCondition.signalAll();
		} catch (InterruptedException ie) {
			// do nothing;
		} catch (Exception ex) {
			log.error(ex);
		} finally {
			if (flag)
				checkerLock.unlock();
		}
	}

	/**
	 * 队列中无任务的阻塞消息
	 * 
	 * @param waittime
	 */
	public void blockNoTimeOutJob(long waittime) {
		if (waittime < 0)
			return;

		boolean flag = checkerLock.tryLock();

		if (flag) {
			try {
				hasJobCondition.await(waittime, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ie) {
				// do nothing;
			} catch (Exception ex) {
				log.error("block Queue error.", ex);
			} finally {
				checkerLock.unlock();
			}
		}

	}

	@Override
	public void run() {
		while (runFlag) {
			try {
				JobFutureTask task = futureTaskTTLList.peek();

				if (task == null) {
					blockNoTimeOutJob(60000);
				} else {
					do {
						if (System.currentTimeMillis() > task.getTimeOut()) {
							task.cancel(true);
							futureTaskTTLList.remove(task);
						} else {
							break;
						}

						task = futureTaskTTLList.peek();
					} while (task != null);

					if (task != null)
						blockNoTimeOutJob(task.getTimeOut()
								- System.currentTimeMillis());
				}
			} catch (Exception ex) {
				log.error(ex);
			}

		}
	}

}
