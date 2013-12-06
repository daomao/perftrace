package org.googlecode.threadpool;

import java.util.Date;

import org.googlecode.threadpool.PoolConfig.TaskConfig.TaskConfigBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TaskCentralExecutorTest {

	private TaskCentralExecutor taskCentralExecutor;
	@Before
	public void setUp() throws Exception {
		PoolConfig poolConfig = new PoolConfig();
		poolConfig.setMaximumPoolSize(10);
		poolConfig.addTaskConfig(TaskConfigBuilder.newInstance("Test").reserve(2).elastic(0).build());
		taskCentralExecutor = TaskCentralExecutor.getInstance(poolConfig);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSubmitTask() {
		taskCentralExecutor.submitTask(new RunnableTask(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("run 1");
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Hello World" + new Date());
			}
		}, "Test", 4980));
		
		taskCentralExecutor.submitTask(new RunnableTask(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("run 2");
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Hello World 12" + new Date());
			}
		}, "Test", 60000));
		try {
			Thread.sleep(1000 * 20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

}
