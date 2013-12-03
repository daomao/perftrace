package org.googlecode.threadpool;

import static org.junit.Assert.fail;

import java.util.Date;

import org.googlecode.threadpool.PoolConfig.TaskConfig;
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
		PoolConfig.setMaximumPoolSize(10);
		PoolConfig.addTaskConfig(new TaskConfig(1, 0, 10, "Test"));
		q = BufferQueue.newInstance("Test",10);
		q.startQueueTaskSubmit(TaskCentralExecutor.newTaskCentralExecutor(), TaskQuotaAllocator.getInstance());
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStartQueueTaskSubmit() {
}

	@Test
	public void testAddTask() {
		q.addTask(new RunnableTask(new Runnable() {
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
		}, "Test"));
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		q.addTask(new RunnableTask(new Runnable() {
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
		}, "Test", 10));
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//q.stop();
	}


	@Test
	public void testNotifyHasResource() {
		fail("Not yet implemented");
	}

}
