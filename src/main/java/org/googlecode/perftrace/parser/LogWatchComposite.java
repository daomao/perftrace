package org.googlecode.perftrace.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhongfeng
 * 
 */
public class LogWatchComposite extends LogWatchComponent {
	/**
	 * 用来存储组合对象中包含的子组件对象
	 */
	private List<LogWatchComponent> childComponents = new ArrayList<LogWatchComponent>();

	/**
	 * 
	 */
	public LogWatchComposite() {
		super();
	}

	/**
	 * @param tag
	 * @param startTime
	 * @param elapsedTime
	 * @param message
	 * @param suffix
	 */
	public LogWatchComposite(String tag, long startTime, long elapsedTime,
			String message, String suffix) {
		super(tag, startTime, elapsedTime, message, suffix);
	}

	@Override
	public void addChild(LogWatchComponent child) {
		childComponents.add(child);
	}

	@Override
	public List<LogWatchComponent> children() {
		return childComponents;
	}

	@Override
	public LogWatchComponent getChild(int index) {
		return childComponents.get(index);
	}

	public List<LogWatchComponent> getChildComponents() {
		return childComponents;
	}

	public void setChildComponents(List<LogWatchComponent> childComponents) {
		this.childComponents = childComponents;
	}

	@Override
	public void removeChild(LogWatchComponent child) {
		childComponents.remove(child);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((childComponents == null) ? 0 : childComponents.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LogWatchComposite other = (LogWatchComposite) obj;
		if (childComponents == null) {
			if (other.childComponents != null)
				return false;
		} else if (!childComponents.equals(other.childComponents))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + " LogWatchComposite [childComponents="
				+ childComponents + "]";
	}
}
