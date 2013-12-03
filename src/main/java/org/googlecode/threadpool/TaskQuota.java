package org.googlecode.threadpool;


/**
 * 线程资源分配模型
 * 
 * @author zhongfeng
 */
public class TaskQuota{
	
	private final String taskKey;
	
	private final Quota reserveQuota;
	
	private final Quota elasticQuota;

	public TaskQuota(String taskKey,int reserveQuota) {
		this(taskKey,reserveQuota,0);
	}

	public TaskQuota(String taskKey,int reserveQuota,int elasticQuota) {
		this.taskKey = taskKey;
		this.reserveQuota = new Quota(reserveQuota);
		this.elasticQuota = new Quota(elasticQuota);
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
