package org.googlecode.threadpool;

import static org.junit.Assert.fail;

import java.util.Date;

import org.googlecode.threadpool.RunnableTask.TaskBuilder;
import org.googlecode.threadpool.TimeoutMonitor.TimeoutChecker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TimeoutCheckerTest {

	private TimeoutChecker checker;
	@Before
	public void setUp() throws Exception {

		 checker = TimeoutChecker.newInstance("Test");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddTask() {
		RunnableTask task = TaskBuilder.newInstance(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Hello World" + new Date());
			}
		}).taskKey("Test").timeout(5000).build();
		checker.start();
		TaskCentralExecutor.getInstance(PoolConfig.DEFAULT_CONFIG).execute(checker.addTask(task));
		try {
			Thread.sleep(1000*20);
			//System.out.println(checker.getTaskDelayQueue().take());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetTaskDelayQueue() {
		fail("Not yet implemented");
	}

}
