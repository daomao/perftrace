package org.googlecode.threadpool;

import java.util.concurrent.Semaphore;

/**
 * @author zhongfeng
 * 
 */
public class Quota {

	/**
	 * 计数许可
	 */
	private Semaphore state;

	/**
	 * 初始值
	 */
	private int value;

	/**
	 * 
	 */
	public Quota() {

	}

	public Quota(int value) {
		if (value < 0)
			throw new IllegalArgumentException("Quota should not less than 0.");
		this.value = value;
		this.state = new Semaphore(value);
	}

	/** @return 当前剩余配额. */
	public int state() {
		return state.availablePermits();
	}

	/**
	 * 占据一个配额.
	 * 
	 * @return false 表示预留的配额以用完, 反之为true.
	 */
	public boolean acquire() {
		return state.tryAcquire();
	}

	/**
	 * 释放一个配额.
	 * 
	 */
	public void release() {
		state.release();
	}

	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "Quota [state=" + state.availablePermits() + ", value=" + value + "]";
	}

}
