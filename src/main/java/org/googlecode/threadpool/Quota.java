package org.googlecode.threadpool;

import java.util.concurrent.Semaphore;

/** {@link Quota} */
public class Quota {
	private  Semaphore state;
	private  int value;

	/**
	 * 
	 */
	public Quota() {
		
	}

	public Quota(int value) {
		if (value < 0)
			throw new IllegalArgumentException(
					"Quota should not less than 0.");
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
	 * @return false 表示无效的释放, 正常情况下不应出现, 反之为true.
	 */
	public void release() {
		state.release();
	}

	public int getValue() {
		return value;
	}

}


