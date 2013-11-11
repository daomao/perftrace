package org.googlecode.threadpool;


/**
 * 线程资源分配模型
 * 
 * @author zhongfeng
 */
public class TaskQuota  extends Quota{

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
	private final QuotaPolicy quotaPolicy;

	public TaskQuota(String taskKey,int quota) {
		this(taskKey,quota,QuotaPolicy.LEAVE);
	}

	public TaskQuota(String taskKey,int quota,QuotaPolicy quotaPolicy) {
		super(quota);
		this.taskKey = taskKey;
		this.quotaPolicy = quotaPolicy;

	}

	public String getTaskKey() {
		return taskKey;
	}
	public QuotaPolicy getQuotaPolicy() {
		return quotaPolicy;
	}



	public boolean isLeavePolicy()
	{
		return QuotaPolicy.LEAVE.equals(getQuotaPolicy());
	}
	
	public boolean isLimitPolicy()
	{
		return QuotaPolicy.LIMIT.equals(getQuotaPolicy());
	}
}
