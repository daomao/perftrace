package org.googlecode.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.googlecode.threadpool.PoolConfig.TaskConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * 
 * 先构建所有的Reserve方式的quota，满足条件：所有的Reserve Quota总和 < maximumPoolSize;
 * 如果剩余的sharedPoolSize小于默认最小值，则分配失败； *
 * 
 * @author zhongfeng
 * 
 */
class TaskQuotaAllocator {

	private static final Logger LOG = LoggerFactory
			.getLogger(TaskQuotaAllocator.class);

	private static TaskQuotaAllocator INSTANCE;

	/**
	 * 
	 */
	private LoadingCache<String, TaskQuota> quotaCache;

	/**
	 * 共享配额
	 */
	private Quota sharedTaskQuota;

	/**
	 * 线程池总大小
	 */
	private int maximumPoolSize;

	/**
	 * PoolConfig保持了全局的配置，TaskQuotaAllocator构建从PoolConfig开始
	 */
	private TaskQuotaAllocator(PoolConfig poolConfig) {
		this.quotaCache = CacheBuilder.newBuilder().build(
				new TaskQuotaCacheLoader(poolConfig));
		this.maximumPoolSize = poolConfig.getMaximumPoolSize();
		// 构建sharedTaskQuota
		this.sharedTaskQuota = buildSharedTaskQuota(poolConfig);
	}

	/**
	 * 
	 * @return totalLeaveYHQuota 分配出去的独占Quota总量
	 * 
	 */
	private int buildReservePolicyTaskQuota(PoolConfig poolConfig) {
		int totalLeaveQuota = 0;
		for (TaskConfig taskCfg : poolConfig.getAllTaskConfig()) {
			TaskQuota taskQuota = new TaskQuota(taskCfg.getTaskKey(), taskCfg
					.getReserve(), taskCfg.getElastic());
			totalLeaveQuota += taskQuota.getReserveQuota().getValue();
			// 校验totalLeaveQuota不能大于maximumPoolSize
			if (totalLeaveQuota > getMaximumPoolSize())
				throw new IllegalArgumentException("TotalLeaveQuota: "
						+ totalLeaveQuota + " is greater than maximumPoolSize:"
						+ getMaximumPoolSize());
			quotaCache.put(taskQuota.getTaskKey(), taskQuota);

		}
		return totalLeaveQuota;
	}


	private Quota buildSharedTaskQuota(PoolConfig poolConfig) {
		int totalLeaveQuota = buildReservePolicyTaskQuota(poolConfig);

		// sharedPoolSize大小等于maximumPoolSize减去所有leave模式占用的
		int sharedPoolSize = (poolConfig.getMaximumPoolSize() - totalLeaveQuota);

		// 如果公用池小于minAvailableSharedPoolSize，初始化报错
		if (sharedPoolSize < poolConfig.getMinAvailableSharedPoolSize())
			throw new IllegalArgumentException("Current SharedPoolSize is: "
					+ sharedPoolSize
					+ " less than minAvailableSharedPoolSize : "
					+ poolConfig.getMinAvailableSharedPoolSize());

		return new Quota(sharedPoolSize);
	}

	public boolean acquire(RunnableTask runnableTask) {
		// 不会取到NULL
		TaskQuota quota = getTaskQuota(runnableTask);
		LOG.debug("Acquire currentQuota : {}", quota);
		List<Quota> taskCurrrentUsedQuota = new ArrayList<Quota>();
		// 优先使用独占资源
		if (quota.getReserveQuota().acquire()) {
			LOG.debug("Use Reserve Resource. Success Quota is : {}", quota);
			taskCurrrentUsedQuota.add(quota.getReserveQuota());
			runnableTask.setCurrentUsedQuota(taskCurrrentUsedQuota);
			runnableTask.setReserve(true);
			return true;
		}

		// 独占资源用完后，尝试竞争共享资源
		boolean limitQuotaAC = quota.getElasticQuota().acquire();
		if (!limitQuotaAC) {
			LOG.debug("GetElasticQuota Fail. Quota is : {}", quota);
			return false;
		}
		boolean flag = false;
		boolean sharedTaskQuotaAc = sharedTaskQuota.acquire();
		// limitQuotaAC && sharedTaskQuotaAc 同时获取成功 flag = true
		if (sharedTaskQuotaAc) {
			LOG.debug("Use Shared Resource. Success Quota is : {}", quota);
			taskCurrrentUsedQuota.add(quota.getElasticQuota());
			taskCurrrentUsedQuota.add(sharedTaskQuota);
			runnableTask.setCurrentUsedQuota(taskCurrrentUsedQuota);
			runnableTask.setReserve(false);
			flag = true;
		} else {
			LOG.debug("SharedTaskQuota Fail. Shared Quota is : {}",
					sharedTaskQuota.state());
			if (limitQuotaAC) {
				quota.getElasticQuota().release();
			}
		}
		return flag;
	}

	/**
	 * @param runnableTask
	 * @param quota
	 * @return
	 */
	private TaskQuota getTaskQuota(RunnableTask runnableTask) {
		return getTaskQuota(runnableTask.getTaskKey());
	}

	public TaskQuota getTaskQuota(String taskKey) {
		try {
			return quotaCache.get(taskKey);
		} catch (ExecutionException e) {
			// 不会发生
			throw new RuntimeException(e);
		}
	}

	public void release(RunnableTask runnableTask) {
		for (Quota quota : runnableTask.getCurrentUsedQuota())
			quota.release();
	}

	public synchronized static TaskQuotaAllocator getInstance(
			PoolConfig poolConfig) {
		if (INSTANCE == null) {
			INSTANCE = new TaskQuotaAllocator(poolConfig);
		}
		return INSTANCE;
	}

	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public final static class TaskQuotaCacheLoader extends
			CacheLoader<String, TaskQuota> {
		private PoolConfig poolConfig;

		/**
		 * @param poolConfig
		 */
		private TaskQuotaCacheLoader(PoolConfig poolConfig) {
			this.poolConfig = poolConfig;
		}

		@Override
		public TaskQuota load(String key) throws Exception {
			// 如果没有配置key的quota，则使用默认的设置；无限抢占共用资源
			TaskConfig taskConfig = poolConfig.getTaskConfig(key);
			return new TaskQuota(taskConfig.getTaskKey(), taskConfig
					.getReserve(), taskConfig.getElastic());
		}
	}
}
