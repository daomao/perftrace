package org.googlecode.threadpool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhongfeng
 * 
 */
public class MessageResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -312319620305003386L;
	private Object bindingContext;
	private List<Object> headers = new ArrayList<Object>();
	private Object body;
	private Object messageID;
	private boolean isFault;

	public MessageResult() {

	}

	@SuppressWarnings("unchecked")
	public <T> T getBody() {
		return (T) body;
	}

	public <T> void setBody(T body) {
		this.isFault = false;
		this.body = body;
	}

	public Object getMessageID() {
		return messageID;
	}

	public void setMessageID(Object messageId) {
		this.messageID = messageId;
	}

	public boolean isFault() {
		return isFault;
	}

	public void setFaultBody(Object fault) {
		this.isFault = true;
		this.body = fault;
	}

	public List<Object> getHeaders() {
		return headers;
	}

	@SuppressWarnings("unchecked")
	public <T> T getBindingContext() {
		return (T) bindingContext;
	}

	public <T> void setBindingContext(T bindingContext) {
		this.bindingContext = bindingContext;
	}
}
