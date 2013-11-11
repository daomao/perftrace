package org.googlecode.threadpool;


/** {@link Submitter} */
public interface Submitter {
	void submit(Runnable task, CentralExecutor executor);
}
