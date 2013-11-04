package org.googlecode.perftrace.stat;

import java.math.BigDecimal;

/**
 * DIFF = sample-old_ttl
 * 
 * smoothed_rtt = old_ttl + a*diff
 * 
 * dev = old_dev + b*(|diff|-old_dev)
 * 
 * timeout = smoothed_rtt + c*dev
 * 
 * dev 是平均（mean deviation）偏差的估计，a的取值范围（0-1）， 用于控制加权平均值影响的快慢程度；
 * b(0-1),用于控制新样本对平均方差影响的快慢程度；
 * 
 * c是控制方差对往返超时时限影响程度的因子；
 * 
 * 
 * @author zhongfeng
 * 
 */
class ComplexTimeoutCalculator {

	public final static BigDecimal DEFAULT_RTTL = new BigDecimal(10);

	public final static BigDecimal DEFAULT_DEV = new BigDecimal(5);

	/**
	 * a 取值1/8
	 */
	private final static BigDecimal a = new BigDecimal(1.00)
			.divide(new BigDecimal(8.00));

	/**
	 * b 取值1/8
	 */
	private final static BigDecimal b = new BigDecimal(1.00)
			.divide(new BigDecimal(4.00));

	/**
	 * c 取值3
	 */
	private final static BigDecimal c = new BigDecimal(3.00);

	private BigDecimal currentRttl = DEFAULT_RTTL;

	private BigDecimal currentDev = DEFAULT_DEV;

	private BigDecimal timeout = currentRttl.add(c.multiply(currentDev));

	ComplexTimeoutCalculator calculate(int sample) {
		BigDecimal diff = new BigDecimal(sample).subtract(currentRttl);
		currentRttl = currentRttl.add(a.multiply(diff));
		currentDev = currentDev
				.add(b.multiply(diff.abs().subtract(currentDev)));
		timeout = currentRttl.add(c.multiply(currentDev));
		return this;
	}

	
	public BigDecimal getCurrentRttl() {
		return currentRttl;
	}


	public void setCurrentRttl(BigDecimal currentRttl) {
		this.currentRttl = currentRttl;
	}


	public BigDecimal getCurrentDev() {
		return currentDev;
	}

	public void setCurrentDev(BigDecimal currentDev) {
		this.currentDev = currentDev;
	}

	public BigDecimal getTimeout() {
		return timeout;
	}

	public void setTimeout(BigDecimal timeout) {
		this.timeout = timeout;
	}

	@Override
	public String toString() {
		return "ComplexTimeoutCalculator [currentDev=" + currentDev
				+ ", currentRttl=" + currentRttl + ", timeout=" + timeout + "]";
	}

	public static void main(String[] args) {
		ComplexTimeoutCalculator cal = new ComplexTimeoutCalculator();
		System.out.println(cal.calculate(10).calculate(1).calculate(1));
	}
}
