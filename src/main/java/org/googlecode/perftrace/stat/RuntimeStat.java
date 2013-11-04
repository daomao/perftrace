package org.googlecode.perftrace.stat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * @author zhongfeng
 * 
 */
class RuntimeStat {

	private final static Logger logger = LoggerFactory
			.getLogger(RuntimeStat.class);

	private String keyStr;

	/**
	 * 每1秒交易处理数
	 */
	private AtomicLong tps = new AtomicLong(0);

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
	 * 请求响应总时间(只统计成功响应)
	 */
	private AtomicLong rtTotal = new AtomicLong(0);

	/**
	 * 平均响应时间（成功响应） rtTotal/tsSuccess;
	 */
	private AtomicLong rt = new AtomicLong(0);

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
		tps.incrementAndGet();
		if (isFault) {
			tsFailed.incrementAndGet();
			rtFailedTotal.addAndGet(elapsedTime);
		} else {
			tsSuccess.incrementAndGet();
			rtTotal.addAndGet(elapsedTime);
		}
	}

	public String getKeyStr() {
		return keyStr;
	}

	public void setKeyStr(String keyStr) {
		this.keyStr = keyStr;
	}

	public AtomicLong getTps() {
		return tps;
	}

	public void setTps(AtomicLong tps) {
		this.tps = tps;
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

	public AtomicLong getRtTotal() {
		return rtTotal;
	}

	public void setRtTotal(AtomicLong rtTotal) {
		this.rtTotal = rtTotal;
	}

	public long getRt() {
		long successNum = tsSuccess.get() == 0L ? 1L : tsSuccess.get();
		return rt.addAndGet(rtTotal.get() / successNum);
	}

	public long getRtFailed() {
		long failedNum = tsFailed.get() == 0L ? 1L : tsFailed.get();
		return rtFailed.addAndGet(rtFailedTotal.get() / failedNum);
	}

	public long getSuccessFailedRatio() {
		long failedNum = tsFailed.get() == 0L ? 1L : tsFailed.get();
		long successNum = tsSuccess.get() == 0L ? 1L : tsSuccess.get();
		return failedNum / successNum;
	}

	public void setRt(AtomicLong rt) {
		this.rt = rt;
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
	 * @return keyStr|tps|currentNum|tsSuccess|rt|tsFailed|rtFailed
	 */
	public String getLogString() {
		return Joiner.on("|").join(
				new Object[] { getKeyTimeSeg(), "TPS:" + tps.get(),
						"T_NUM:" + currentNum.get(),
						"TPSOK:" + tsSuccess.get(), "RT:" + getRt(),
						tsFailed.get(), getRtFailed() });
	}

	/**
	 * 失败成功数大于9或者并发大于0并且成功个数
	 * 
	 * @return
	 */
	public boolean isBadStat() {
		if ((getSuccessFailedRatio() > 9L)
				|| ((currentNum.get() > 0) && (getTsSuccess().get() == 0L)))
			return true;
		return false;
	}

	/**
	 * @return
	 */
	private String getKeyTimeSeg() {
		return keyStr;
	}
}
