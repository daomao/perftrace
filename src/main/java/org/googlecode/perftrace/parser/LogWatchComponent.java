package org.googlecode.perftrace.parser;

import java.util.List;

/**
 * @author zhongfeng
 * 
 */
public abstract class LogWatchComponent {
	protected String tag;
	protected long startTime;
	protected long elapsedTime;
	protected String message;
	protected String suffix;

	/**
	 * 
	 */
	public LogWatchComponent() {
	}

	/**
	 * @param tag
	 * @param startTime
	 * @param elapsedTime
	 * @param message
	 * @param suffix
	 */
	public LogWatchComponent(String tag, long startTime, long elapsedTime,
			String message, String suffix) {
		this.tag = tag;
		this.startTime = startTime;
		this.elapsedTime = elapsedTime;
		this.message = message;
		this.suffix = suffix;
	}

	/**
	 * 向组合对象中加入组件对象
	 * 
	 * @param child
	 *            被加入组合对象中的组件对象
	 */
	public void addChild(LogWatchComponent child) {
		throw new UnsupportedOperationException("UnsupportedOperation");
	}

	/**
	 * 从组合对象中移出某个组件对象
	 * 
	 * @param child
	 *            被移出的组件对象
	 */
	public void removeChild(LogWatchComponent child) {
		throw new UnsupportedOperationException("UnsupportedOperation");
	}

	/**
	 * 返回某个索引对应的组件对象
	 * 
	 * @param index
	 *            需要获取的组件对象的索引，索引从0开始
	 * @return 索引对应的组件对象
	 */
	public LogWatchComponent getChild(int index) {
		throw new UnsupportedOperationException("UnsupportedOperation");
	}

	public List<LogWatchComponent> children() {
		throw new UnsupportedOperationException("UnsupportedOperation");
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (elapsedTime ^ (elapsedTime >>> 32));
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + (int) (startTime ^ (startTime >>> 32));
		result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LogWatchComponent other = (LogWatchComponent) obj;
		if (elapsedTime != other.elapsedTime)
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (startTime != other.startTime)
			return false;
		if (suffix == null) {
			if (other.suffix != null)
				return false;
		} else if (!suffix.equals(other.suffix))
			return false;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		return true;
	}

	public String toXMLLogString() {
		return null;
		// XStream xstream = new XStream();
		// return xstream.toXML(this);
	}

	public String toJSONLogString() {
		return "";
	}
}
