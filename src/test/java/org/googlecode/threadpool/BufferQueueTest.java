package org.googlecode.threadpool;

import static org.junit.Assert.fail;

import java.util.Date;

import org.googlecode.threadpool.PoolConfig.TaskConfig.TaskConfigBuilder;
import org.googlecode.threadpool.RunnableTask.TaskBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author zhongfeng
 * 
 */
public class BufferQueueTest {

	private BufferQueue q;

	@Before
	public void setUp() throws Exception {
		PoolConfig poolConfig = new PoolConfig();
		poolConfig.setMaximumPoolSize(10);
		poolConfig.addTaskConfig(TaskConfigBuilder.newInstance("Test").reserve(2).elastic(0).build());
		q = BufferQueue.newInstance("Test", 10);
		q.startQueueTaskSubmit(TaskCentralExecutor.getInstance(poolConfig));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStartQueueTaskSubmit() {
	}

	@Test
	public void testAddTask() {
		q.addTask(TaskBuilder.newInstance(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Hello World" + new Date());
			}
		}).taskKey("Test").build());
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		q.addTask(TaskBuilder.newInstance(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Hello World" + new Date());
			}
		}).taskKey("Test").timeout(10).build());
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// q.stop();
	}

	@Test
	public void testNotifyHasResource() {
		fail("Not yet implemented");
	}

}
