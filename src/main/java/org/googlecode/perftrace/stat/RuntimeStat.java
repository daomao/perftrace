package org.googlecode.perftrace.stat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Joiner;

/**
 * @author zhongfeng
 * 
 */
class RuntimeStat {

	private String keyStr;

	/**
	 * 每秒交易失败数
	 */
	private AtomicLong tsFailed = new AtomicLong(0);

	/**
	 * 每秒交易成功数
	 */
	private AtomicLong tsSuccess = new AtomicLong(0);

	/**
	 * 当前并发数
	 */
	private AtomicInteger currentNum = new AtomicInteger(0);

	/**
	 * 请求成功响应总时间
	 */
	private AtomicLong rtSuccessTotal = new AtomicLong(0);

	/**
	 * 平均响应时间（成功响应） rtSuccessTotal/tsSuccess;
	 */
	private AtomicLong rtSuccess = new AtomicLong(0);

	/**
	 * 请求响应总时间(统计失败响应)
	 */
	private AtomicLong rtFailedTotal = new AtomicLong(0);

	/**
	 * 平均响应时间（失败响应） rtFailedTotal/tsFailed;
	 */
	private AtomicLong rtFailed = new AtomicLong(0);

	/**
	 * 
	 */
	public RuntimeStat() {
	}

	/**
	 * @param keyStr
	 */
	public RuntimeStat(String keyStr) {
		this.keyStr = keyStr;
	}

	public void start(int num) {
		currentNum.set(num);
	}

	public void stop(long elapsedTime, boolean isFault, int num) {
		currentNum.set(num);
		if (isFault) {
			tsFailed.incrementAndGet();
			rtFailedTotal.addAndGet(elapsedTime);
		} else {
			tsSuccess.incrementAndGet();
			rtSuccessTotal.addAndGet(elapsedTime);
		}
	}

	public String getKeyStr() {
		return keyStr;
	}

	public void setKeyStr(String keyStr) {
		this.keyStr = keyStr;
	}

	public long getTps() {
		return tsSuccess.get() + tsFailed.get();
	}

	public AtomicLong getTsFailed() {
		return tsFailed;
	}

	public void setTsFailed(AtomicLong tsFailed) {
		this.tsFailed = tsFailed;
	}

	public AtomicLong getTsSuccess() {
		return tsSuccess;
	}

	public void setTsSuccess(AtomicLong tsSuccess) {
		this.tsSuccess = tsSuccess;
	}

	public AtomicInteger getCurrentNum() {
		return currentNum;
	}

	public void setCurrentNum(AtomicInteger currentNum) {
		this.currentNum = currentNum;
	}

	public long getRtTotal() {
		return rtSuccessTotal.get() + rtFailedTotal.get();
	}

	public long getRt() {
		return getRtTotal() / getTps();
	}

	public long getRtFailed() {
		long failedNum = tsFailed.get() == 0L ? 1L : tsFailed.get();
		return rtFailed.addAndGet(rtFailedTotal.get() / failedNum);
	}

	public AtomicLong getRtSuccessTotal() {
		return rtSuccessTotal;
	}

	public long getRtSuccess() {
		long successNum = tsSuccess.get() == 0L ? 1L : tsSuccess.get();
		return rtSuccess.addAndGet(rtSuccessTotal.get() / successNum);
	}

	public AtomicLong getRtFailedTotal() {
		return rtFailedTotal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((keyStr == null) ? 0 : keyStr.hashCode());
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
		RuntimeStat other = (RuntimeStat) obj;
		if (keyStr == null) {
			if (other.keyStr != null)
				return false;
		} else if (!keyStr.equals(other.keyStr))
			return false;
		return true;
	}

	/**
	 * @return keyStr|currentNum|tps|rt|tsSuccess|rtSuccess|tsFailed|rtFailedtsFailed|rtFailed
	 */
	public String getLogString() {
		return Joiner.on("|").join(
				new Object[] { getKeyTimeSeg(), currentNum.get(), getTps(),
						getRt(), tsSuccess.get(), rtSuccess.get(),
						tsFailed.get(), getRtFailed() });
	}

	/**
	 * @return
	 */
	private String getKeyTimeSeg() {
		return keyStr;
	}
}
