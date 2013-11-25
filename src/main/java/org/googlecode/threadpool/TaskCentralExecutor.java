package org.googlecode.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author Administrator
 *
 */
public class TaskCentralExecutor extends ThreadPoolExecutor {

	private JobDispatcher jobDispatcher;

	public TaskCentralExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
			JobDispatcher jobDispatcher) {
		ExecutorService 
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
