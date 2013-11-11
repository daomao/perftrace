package org.googlecode.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * 扩展线程池基本实现， 在每个任务结束后需要减去任务计数器的值
 * 
 * @author fangweng
 */
public class TaskCentralExecutor extends ThreadPoolExecutor {

	private JobDispatcher jobDispatcher;

	public TaskCentralExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
			JobDispatcher jobDispatcher) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
		this.jobDispatcher = jobDispatcher;
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		if (r instanceof Task) {
			jobDispatcher.releaseJob((Task) r);
		}
	}
	
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		// TODO Auto-generated method stub
		
		if (r instanceof Task)
		{
			jobDispatcher.beforeExecuteJob((Task)r);
		}
	}

}
