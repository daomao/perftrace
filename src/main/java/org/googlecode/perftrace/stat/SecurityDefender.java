package org.googlecode.perftrace.stat;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhongfeng
 * 
 */
public class SecurityDefender {

	/**
	 * logger.
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(SecurityDefender.class);

	private static final int DEFAULT_CONTINUE_FAILED_NUM = 10;

	private boolean ctrlFlag = false;

	private Semaphore available = new Semaphore(1);

	private int failNum = 0;

	public synchronized DefenderResult checkPass() {
		if (!ctrlFlag) {
			return DefenderResult.NO_CTRL;
		}
		if (ctrlFlag && available.tryAcquire()) {
			// logger.info("SecurityDefender tryAcquire Pass");
			return DefenderResult.CTRL_PASS;
		}
		return DefenderResult.ACCESS_DENY;
	}

	public void checkStatus(DefenderResult defenderRet, boolean isFault) {
		if (defenderRet.isRelease()) {
			release();
		}
		synchronized (this) {
			if (isFault) {
				if ((failNum++) > DEFAULT_CONTINUE_FAILED_NUM)
					ctrlFlag = true;
			} else {
				failNum = 0;
				ctrlFlag = false;
			}
		}
	}

	private void release() {
		available.release();
	}

	public static class DefenderResult {

		/**
		 * 没有开启访问控制，直接通过
		 */
		public static final DefenderResult NO_CTRL = new DefenderResult(true,
				false);

		/**
		 * 开启访问控制，被拒绝
		 */
		public static final DefenderResult ACCESS_DENY = new DefenderResult(
				false, false);

		/**
		 * 开启访问控制，获取访问令牌
		 */
		public static final DefenderResult CTRL_PASS = new DefenderResult(true,
				true);

		private boolean isPass = false;

		private boolean isRelease = false;

		/**
		 * @param isPass
		 * @param isRelease
		 */
		private DefenderResult(boolean isPass, boolean isRelease) {
			this.isPass = isPass;
			this.isRelease = isRelease;
		}

		public boolean isPass() {
			return isPass;
		}

		public void setPass(boolean isPass) {
			this.isPass = isPass;
		}

		public boolean isRelease() {
			return isRelease;
		}

		public void setRelease(boolean isRelease) {
			this.isRelease = isRelease;
		}

	}

	public static class Hello {
		private String msg;

		/**
		 * @param msg
		 */
		public Hello(String msg) {
			this.msg = msg;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

	}

	public static void main(String[] args) throws Exception, ExecutionException {
		Semaphore sem = new Semaphore(5);
		sem.release(10);
		System.out.println(sem.availablePermits());
		Callable<String> call = new Callable<String>() {

			@Override
			public String call() throws Exception {
				Thread.sleep(5 * 1000);
				return new Hello("test").getMsg();
			}
		};
		FutureTask<String> ft = new FutureTask<String>(call);
		ft.run();
		ft.get();
	}
}
