package org.googlecode.threadpool;


/**
 * 线程资源分配模型
 * 
 * @author zhongfeng
 */
public class TaskQuota{

	public enum QuotaPolicy {
		/**
		 * 预留模式，整个线程池将预留设置数量的线程仅仅用于某一类请求使用
		 */
		LEAVE,
		/**
		 * 限制模式，这类请求使用默认线程池，但不能超过设置的最大值
		 */
		LIMIT};

	private final String taskKey;
	private final Quota reserveQuota;
	private final Quota elasticQuota;
	//private final QuotaPolicy quotaPolicy;

	public TaskQuota(String taskKey,int reserveQuota) {
		this(taskKey,reserveQuota,0);
	}

	public TaskQuota(String taskKey,int reserveQuota,int elasticQuota) {
		this.taskKey = taskKey;
		this.reserveQuota = new Quota(reserveQuota);
		this.elasticQuota = new Quota(elasticQuota);
		//this.quotaPolicy = quotaPolicy;

	}

	public Quota getReserveQuota() {
		return reserveQuota;
	}

	public Quota getElasticQuota() {
		return elasticQuota;
	}

	public String getTaskKey() {
		return taskKey;
	}
}
