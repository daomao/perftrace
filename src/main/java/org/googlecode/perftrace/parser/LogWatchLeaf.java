package org.googlecode.perftrace.parser;

/**
 * @author zhongfeng
 *
 */
public class LogWatchLeaf extends LogWatchComponent {

	/**
	 * 
	 */
	public LogWatchLeaf() {
		super();
	}

	/**
	 * @param tag
	 * @param startTime
	 * @param elapsedTime
	 * @param message
	 * @param suffix
	 */
	public LogWatchLeaf(String tag, long startTime, long elapsedTime,
			String message, String suffix) {
		super(tag, startTime, elapsedTime, message, suffix);
	}
}
