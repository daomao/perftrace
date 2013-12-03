package org.googlecode.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.googlecode.threadpool.PoolConfig.TaskConfig;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * 
 * 先构建所有的Reserve方式的quota，满足条件：所有的Reserve Quota总和 < maximumPoolSize;
 * 如果剩余的sharedPoolSize小于默认最小值，则分配失败；
 *  * 
 * @author zhongfeng
 * 
 */
public class TaskQuotaAllocator {

	/**
	 * 单例模式
	 */
	private final static TaskQuotaAllocator INSTANCE = new TaskQuotaAllocator();

	/**
	 * 
	 */
	private LoadingCache<String, TaskQuota> quotaCache = CacheBuilder
			.newBuilder().build(new CacheLoader<String, TaskQuota>() {
				@Override
				public TaskQuota load(String key) throws Exception {
					//如果没有配置key的quota，则使用默认的设置；无限抢占共用资源
					TaskConfig taskConfig = PoolConfig.getTaskConfig(key);
					return new TaskQuota(taskConfig.getTaskKey(), taskConfig
							.getReserve(), taskConfig.getElastic());
				}
			});

	/**
	 * 共享配额
	 */
	private Quota sharedTaskQuota;

	/**
	 * 线程池总大小
	 */
	private int maximumPoolSize;

	/**
	 * 共享池大小（弹性配额共享）
	 */
	private int sharedPoolSize;

	/**
	 * PoolConfig保持了全局的配置，TaskQuotaAllocator构建从PoolConfig开始
	 */
	private TaskQuotaAllocator() {
		List<TaskQuota> quotaList = new ArrayList<TaskQuota>();
		for (TaskConfig taskCfg : PoolConfig.getAllTaskConfig()) {
			quotaList.add(new TaskQuota(taskCfg.getTaskKey(), taskCfg
					.getReserve(), taskCfg.getElastic()));
		}
		init(PoolConfig.getMaximumPoolSize(), PoolConfig
				.getMinAvailableSharedPoolSize(), quotaList);
	}

	private void init(int maximumPoolSize, int minAvailableSharedPoolSize,
			List<TaskQuota> quotaList) {
		this.maximumPoolSize = maximumPoolSize;

		int totalLeaveQuota = buildReservePolicyTaskQuota(quotaList);

		int sharedPoolSize = caculateSharedPoolSize(minAvailableSharedPoolSize,
				totalLeaveQuota);

		// 构建sharedTaskQuota
		this.sharedTaskQuota = new Quota(sharedPoolSize);
	}

	/**
	 * @param quotaList
	 * @return totalLeaveYHQuota 分配出去的独占Quota总量
	 * 
	 */
	private int buildReservePolicyTaskQuota(List<TaskQuota> quotaList) {
		int totalLeaveQuota = 0;
		for (TaskQuota taskQuota : quotaList) {
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

	/**
	 * @param minAvailableSharedPoolSize
	 * @param totalLeaveQuota
	 */
	private int caculateSharedPoolSize(int minAvailableSharedPoolSize,
			int totalLeaveQuota) {
		// sharedPoolSize大小等于maximumPoolSize减去所有leave模式占用的
		this.sharedPoolSize = (getMaximumPoolSize() - totalLeaveQuota);

		// 如果公用池小于minAvailableSharedPoolSize，初始化报错
		if (getSharedPoolSize() < minAvailableSharedPoolSize)
			throw new IllegalArgumentException("Current SharedPoolSize is: "
					+ getSharedPoolSize()
					+ " less than minAvailableSharedPoolSize : "
					+ minAvailableSharedPoolSize);
		return this.sharedPoolSize;
	}

	public boolean acquire(RunnableTask runnableTask) {
		//不会取到NULL
		TaskQuota quota = getTaskQuota(runnableTask);
		List<Quota> taskCurrrentUsedQuota = new ArrayList<Quota>();
		//优先使用独占资源
		if (quota.getReserveQuota().acquire()) {
			taskCurrrentUsedQuota.add(quota.getReserveQuota());
			runnableTask.setCurrentUsedQuota(taskCurrrentUsedQuota);
			runnableTask.setReserve(true);
			return true;
		}

		// 独占资源用完后，尝试竞争共享资源
		boolean limitQuotaAC = quota.getElasticQuota().acquire();
		if (!limitQuotaAC) {
			return false;
		}
		boolean flag = false;
		boolean sharedTaskQuotaAc = sharedTaskQuota.acquire();
		//limitQuotaAC && sharedTaskQuotaAc 同时获取成功 flag = true
		if (sharedTaskQuotaAc) {
			taskCurrrentUsedQuota.add(quota.getElasticQuota());
			taskCurrrentUsedQuota.add(sharedTaskQuota);
			runnableTask.setCurrentUsedQuota(taskCurrrentUsedQuota);
			runnableTask.setReserve(false);
			flag = true;
		} else {
			if (limitQuotaAC)
				quota.getElasticQuota().release();
		}
		return flag;
	}

	/**
	 * @param runnableTask
	 * @param quota
	 * @return
	 */
	private TaskQuota getTaskQuota(RunnableTask runnableTask ){
		try {
			return quotaCache.get(runnableTask.getTaskKey());
		} catch (ExecutionException e) {
			//不会发生
			throw new RuntimeException(e);
		}
	}

	public void release(RunnableTask runnableTask) {
		for (Quota quota : runnableTask.getCurrentUsedQuota())
			quota.release();
	}

	public static TaskQuotaAllocator getInstance() {
		return INSTANCE;
	}

	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public int getSharedPoolSize() {
		return sharedPoolSize;
	}

}
