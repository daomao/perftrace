package org.googlecode.threadpool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TaskQuota为LIMIT方式时，其quota不能大于sharedPoolSize; 因此构建时必须一次性构建好状态
 * 
 * (1)先构建所有的LEAVE方式的quota，满足条件：所有的LEAVE Quota总和 < maximumPoolSize;
 * 如果剩余的sharedPoolSize小于默认最小值，则分配失败；
 * 
 * (2)所有为LIMIT的quota，其值不得大于sharedPoolSize;
 * 
 * @author zhongfeng
 * 
 */
public class TaskQuotaFactory {

	private Map<String, TaskQuota> quotaCache = new HashMap<String, TaskQuota>();

	private Quota sharedTaskQuota;

	private int maximumPoolSize;

	private int sharedPoolSize;

	public void init(int maximumPoolSize, int minAvailableSharedPoolSize,
			List<TaskQuota> quotaList) {
		this.maximumPoolSize = maximumPoolSize;

		int totalLeaveQuota = buildLeavePolicyTaskQuota(quotaList);

		int sharedPoolSize = caculateSharedPoolSize(minAvailableSharedPoolSize,
				totalLeaveQuota);

		// 构建sharedTaskQuota
		this.sharedTaskQuota = new Quota(sharedPoolSize);
		//
		buildLimitPolicyTaskQuota(quotaList, sharedPoolSize);
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

	/**
	 * @param quotaList
	 * @return totalLeaveQuota 分配出去的独占Quota总量
	 * 
	 */
	private int buildLeavePolicyTaskQuota(List<TaskQuota> quotaList) {
		int totalLeaveQuota = 0;
		for (TaskQuota taskQuota : quotaList) {
			if (taskQuota.isLeavePolicy()) {
				totalLeaveQuota += taskQuota.getValue();
				// 校验totalLeaveQuota不能大于maximumPoolSize
				if (totalLeaveQuota > getMaximumPoolSize())
					throw new IllegalArgumentException("TotalLeaveQuota: "
							+ totalLeaveQuota
							+ " is greater than maximumPoolSize:"
							+ getMaximumPoolSize());
				quotaCache.put(taskQuota.getTaskKey(), taskQuota);
			}
		}
		return totalLeaveQuota;
	}

	/**
	 * LIMIT POLICY的 task quota，其值不能大于SharedPoolSize
	 * 
	 * @param quotaList
	 */
	private void buildLimitPolicyTaskQuota(List<TaskQuota> quotaList,
			int sharedPoolSize) {
		for (TaskQuota taskQuota : quotaList) {
			if (taskQuota.isLimitPolicy()) {
				if (taskQuota.getValue() > sharedPoolSize) {
					throw new IllegalArgumentException("TaskQutoa: "
							+ taskQuota + " greater than SharedPoolSize : "
							+ sharedPoolSize);
				}
				quotaCache.put(taskQuota.getTaskKey(), taskQuota);
			}
		}
	}

	public boolean acquire(Task task) {
		// 如果quota為null 怎麼處理？
		TaskQuota quota = quotaCache.get(task.getTaskKey());
		List<Quota> taskCurrrentUsedQuota = new ArrayList<Quota>();
		if (quota.isLeavePolicy()) {
			if (quota.acquire()) {
				taskCurrrentUsedQuota.add(quota);
				task.setCurrentUsedQuota(taskCurrrentUsedQuota);
				return true;
			}
			if (sharedTaskQuota.acquire()) {
				taskCurrrentUsedQuota.add(sharedTaskQuota);
				task.setCurrentUsedQuota(taskCurrrentUsedQuota);
				return true;
			}
		}
		if (quota.isLimitPolicy()) {
			boolean limitQuotaAC = quota.acquire();
			boolean sharedTaskQuotaAc = sharedTaskQuota.acquire();
			if (limitQuotaAC && sharedTaskQuotaAc) {
				taskCurrrentUsedQuota.add(quota);
				taskCurrrentUsedQuota.add(sharedTaskQuota);
				task.setCurrentUsedQuota(taskCurrrentUsedQuota);
				return true;
			}
			if (limitQuotaAC)
				quota.release();
			if (sharedTaskQuotaAc)
				sharedTaskQuota.release();
		}
		return false;
	}

	public void release(Task task) {
		for (Quota quota : task.getCurrentUsedQuota())
			quota.release();
	}

	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public void setMaximumPoolSize(int maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}

	public int getSharedPoolSize() {
		return sharedPoolSize;
	}

	public void setSharedPoolSize(int sharedPoolSize) {
		this.sharedPoolSize = sharedPoolSize;
	}

}
