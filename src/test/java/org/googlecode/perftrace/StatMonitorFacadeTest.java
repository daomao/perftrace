package org.googlecode.perftrace;


import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.googlecode.perftrace.stat.StatMonitorFacade;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StatMonitorFacadeTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testStatMonitor() throws InterruptedException
	{
		ExecutorService exec = Executors.newCachedThreadPool();
		Runnable task = new Runnable() {
			
			@Override
			public void run() {
				for(;;)
				{
				StatMonitorFacade.start("test");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				StatMonitorFacade.stop("test", 5L, false);
				}
			}
		};
		
		Runnable task2 = new Runnable() {
			
			@Override
			public void run() {
				for(;;)
				{
				StatMonitorFacade.start("test");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				StatMonitorFacade.stop("test", 5L, true);
				}
			}
		};
		Runnable task3 = new Runnable() {
			
			@Override
			public void run() {
				for(;;)
				{
				StatMonitorFacade.start("test1333");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				StatMonitorFacade.stop("test1333", 5L, true);
				}
			}
		};
		for(int i = 0;i<10;i++)
		{
		exec.submit(task);
		}
		
		for(int i = 0;i<5;i++)
		{
		exec.submit(task2);
		}
		for(int i = 0;i<10;i++)
		{
		exec.submit(task3);
		}
		exec.awaitTermination(100, TimeUnit.SECONDS);
		exec.shutdown();
	}

}
