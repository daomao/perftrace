package org.googlecode.perftrace.stat;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalListeners;
import com.google.common.cache.RemovalNotification;

/**
 * @author zhongfeng
 * 
 */
public class StatMonitor {

	private final static Logger logger = LoggerFactory
			.getLogger(StatMonitor.class);

	private static final int DEFAULT_NUM = 1;

	private final static Executor LOG_EXEC = Executors
			.newSingleThreadExecutor();

	private String serviceName;

	private int maxNum = DEFAULT_NUM;

	private LoadingCache<String, RuntimeStat> statCache;

	private volatile RuntimeStat currentStat;

	/**
	 * 当前并发数
	 */
	private AtomicInteger currentNum = new AtomicInteger(0);

	/**
	 * @param serviceName
	 */
	public StatMonitor(String serviceName) {
		this(serviceName, DEFAULT_NUM);
	}

	/**
	 * @param serviceName
	 */
	public StatMonitor(String serviceName, int maxNum) {
		this.serviceName = serviceName;
		this.maxNum = maxNum;
		this.statCache = initStatCache(maxNum);
	}

	private final static Logger STAT_LOGGER = LoggerFactory
			.getLogger("org.googlecode.statmonitor.statlog");

	/**
	 * 
	 * @param maxNum
	 * 
	 */
	private LoadingCache<String, RuntimeStat> initStatCache(int maxNum) {
		RemovalListener<String, RuntimeStat> removalListener = new RemovalListener<String, RuntimeStat>() {

			@Override
			public void onRemoval(
					RemovalNotification<String, RuntimeStat> notification) {
				STAT_LOGGER.info("{}", notification.getValue().getLogString());
			}
		};

		CacheLoader<String, RuntimeStat> runStatsLoader = new CacheLoader<String, RuntimeStat>() {
			@Override
			public RuntimeStat load(String key) throws Exception {
				return new RuntimeStat(key);
			}
		};

		return CacheBuilder.newBuilder().maximumSize(maxNum).expireAfterAccess(
				DEFAULT_NUM, TimeUnit.SECONDS).removalListener(
				RemovalListeners.asynchronous(removalListener, LOG_EXEC))
				.build(runStatsLoader);
	}

	public void start() {
		RuntimeStat runtimeStat = getCurrentRuntimeStat();
		if (runtimeStat != null) {
			runtimeStat.start(currentNum.incrementAndGet());
			setCurrentStat(runtimeStat);
		}
	}

	public void stop(long elapsedTime, boolean isFault) {
		RuntimeStat runtimeStat = getCurrentRuntimeStat();
		if (runtimeStat != null) {
			runtimeStat
					.stop(elapsedTime, isFault, currentNum.decrementAndGet());
			setCurrentStat(runtimeStat);
		}
		// logger.info("------------"+runtimeStat.getLogString());
	}

	/**
	 * @return
	 * @throws ExecutionException
	 */
	private RuntimeStat getCurrentRuntimeStat() {
		//使用缓存的时间，提高效率
		Date date = new Date(SystemTimer.currentTimeMillis());
		String key = KeyStrategy.getKey(serviceName, date);
		RuntimeStat runtimeStat = null;
		try {
			runtimeStat = statCache.get(key);
		} catch (ExecutionException e) {
			logger.error("Init error", e);
		}
		return runtimeStat;
	}

	public Collection<RuntimeStat> getLatestStatArray() {
		return Collections.unmodifiableCollection(getStatCache().asMap()
				.values());
	}

	public static class KeyStrategy {

		/**
		 * 格式：精确到秒
		 */
		private final static String DATE_FORMAT_PATTERN = "yyyy-MM-dd-HH:mm:ss";

		/**
		 * SimpleDateFormat非线程安全
		 */
		private static ThreadLocal<SimpleDateFormat> TL_SDF = new ThreadLocal<SimpleDateFormat>() {
			protected SimpleDateFormat initialValue() {
				return new SimpleDateFormat(DATE_FORMAT_PATTERN);
			}
		};

		public static String getKey(String tag, Date date) {
			return Joiner.on("|").join(
					Arrays.asList(tag, TL_SDF.get().format(date)));
		}
	}

	/**
	 * 时间缓存
	 * 
	 * @author zhongfeng
	 * 
	 */
	public static class SystemTimer {
		private final static ScheduledExecutorService executor = Executors
				.newSingleThreadScheduledExecutor();

		private static final long tickUnit = Long.parseLong(System.getProperty(
				"notify.systimer.tick", "50"));

		static {
			executor.scheduleAtFixedRate(new TimerTicker(), tickUnit, tickUnit,
					TimeUnit.MILLISECONDS);
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					executor.shutdown();
				}
			});
		}

		private static volatile long time = System.currentTimeMillis();

		private static class TimerTicker implements Runnable {
			public void run() {
				time = System.currentTimeMillis();
			}
		}

		public static long currentTimeMillis() {
			return time;
		}

	}

	public int getMaxNum() {
		return maxNum;
	}

	public void setMaxNum(int maxNum) {
		this.maxNum = maxNum;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public LoadingCache<String, RuntimeStat> getStatCache() {
		return statCache;
	}

	public void setStatCache(LoadingCache<String, RuntimeStat> statCache) {
		this.statCache = statCache;
	}

	public RuntimeStat getCurrentStat() {
		return currentStat;
	}

	public void setCurrentStat(RuntimeStat currentStat) {
		this.currentStat = currentStat;
	}

	public static void main(String[] args) throws InterruptedException {
		ExecutorService exec = Executors.newCachedThreadPool();
		final StatMonitor statMgr = new StatMonitor("TEST");
		for (int i = 0; i < 100; i++) {
			exec.execute(new Runnable() {

				@Override
				public void run() {
					while (true) {
						statMgr.start();
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						statMgr.stop(1000, false);
					}
				}
			});
		}
		exec.awaitTermination(1, TimeUnit.DAYS);
	}

}
