package test.btrace;

import com.sun.btrace.annotations.*;
import static com.sun.btrace.BTraceUtils.*;
import java.awt.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 33.* A BTrace program that can be run against a GUI 34.* program. This
 * program prints (monotonic) count of 35.* number of java.awt.Components
 * created once every 36.* 2 seconds (2000 milliseconds). 37.
 */
@BTrace
public class NewComponent {
	// component count
	private static volatile long count;

	@OnMethod(clazz = "java.awt.Component", method = "<init>", location = @Location(Kind.RETURN))
	public static void onnew(@Self Component c) {
		// increment counter on constructor entry
		count++;
	}

	@OnTimer(2000)
	public static void print() {
		// print the counter
		println(Strings.strcat("component count = ", str(count)));
	}

	public static class Task implements Runnable {
		private Semaphore sem = new Semaphore(0);
		private String name;
		private int printCount;
		private Semaphore nextSem;

		/**
		 * @param name
		 */
		public Task(String name, int printCount, Semaphore nextSem) {
			this.name = name;
			this.printCount = printCount;
			this.nextSem = nextSem;
		}

		public Semaphore getSem() {
			return sem;
		}

		public void setSem(Semaphore sem) {
			this.sem = sem;
		}

		public void run() {
			for(int i = 0;i < printCount;i++)
			{
				try {
					sem.acquire();
				} catch (InterruptedException e) {
				}
				System.out.println(name);
				if(nextSem != null)
					nextSem.release();
			}
		}
	};
	public static void main(String[] args) throws Exception
	{
		Semaphore sem = new Semaphore(0);
		Task c = new Task("C", 10, sem);
		Task b = new Task("B", 10, c.getSem());
		Task a = new Task("A", 10, b.getSem());
		Semaphore startSem = a.getSem();
		ExecutorService exec = Executors.newCachedThreadPool();
		exec.execute(c);
		exec.execute(b);
		exec.execute(a);
		for(int i = 0;i< 10;i++)
		{
			startSem.release();
			sem.acquire();
		}
	//	exec.awaitTermination(1000000, TimeUnit.SECONDS);
		exec.shutdown();
	}
}
