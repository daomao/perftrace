package org.googlecode.perftrace.stat;

import java.math.BigDecimal;

/**
 * @author zhongfeng
 * 
 */
class SimpleTimeoutCalculator {

	public final static BigDecimal DEFAULT_RTTL = new BigDecimal(15000);

	/**
	 * alfa 取值7/8
	 */
	private final static BigDecimal alfa = new BigDecimal(7.00)
			.divide(new BigDecimal(8.00));

	/**
	 * b 取值2
	 */
	private final static BigDecimal b = new BigDecimal(2.00);

	private BigDecimal currentTTL = DEFAULT_RTTL;

	private BigDecimal timeout = b.multiply(currentTTL);

	/**
	 * 
	 */
	public SimpleTimeoutCalculator() {
		this(DEFAULT_RTTL.longValue());
	}

	/**
	 * @param currentTTL
	 */
	public SimpleTimeoutCalculator(long currentTTL) {
		this.currentTTL = new BigDecimal(currentTTL);
		this.timeout = b.multiply(this.currentTTL);
	}

	/**
	 * alfa * currentTTL + (1-alfa)*roundTripSample
	 * 
	 * @param roundTripSample
	 * @return
	 */
	public SimpleTimeoutCalculator calculate(long roundTripSample) {
		currentTTL = (alfa.multiply(currentTTL).add((new BigDecimal(1)
				.subtract(alfa)).multiply(new BigDecimal(roundTripSample))));
		timeout = b.multiply(currentTTL);
		return this;
	}

	public long getRTTL() {
		return getCurrentTTL().setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
	}

	public BigDecimal getCurrentTTL() {
		return currentTTL;
	}

	public void setCurrentTTL(BigDecimal currentTTL) {
		this.currentTTL = currentTTL;
	}

	public BigDecimal getTimeout() {
		return timeout;
	}

	public void setTimeout(BigDecimal timeout) {
		this.timeout = timeout;
	}

	@Override
	public String toString() {
		return "SimpleTimeoutCalculator [currentTTL=" + currentTTL
				+ ", timeout=" + timeout + "]";
	}

	public static void main(String[] args) {
		SimpleTimeoutCalculator cal = new SimpleTimeoutCalculator();
		System.out.println(cal.calculate(10).calculate(10).calculate(5));
	}
}
