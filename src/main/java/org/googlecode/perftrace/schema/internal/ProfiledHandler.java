package org.googlecode.perftrace.schema.internal;

import java.lang.annotation.Annotation;

import org.googlecode.perftrace.DefaultGProfiled;
import org.googlecode.perftrace.GProfiled;
import org.googlecode.perftrace.schema.PerftraceConfig.PatternConf.Pelement.Profiled;

/**
 * @author zhongfeng
 * 
 */
public class ProfiledHandler {

	private GProfiled gProfiled = DefaultGProfiled.getInstance();

	private MethodMatcherHandler methodMatcherHandler = new MethodMatcherHandler();

	public GProfiled getProfiled() {
		return gProfiled;
	}

	public void setProfiled(GProfiled gProfiled) {
		this.gProfiled = gProfiled;
	}

	public void setProfiled(final Profiled prof) {
		this.gProfiled = new GProfiled() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return GProfiled.class;
			}

			@Override
			public long timeThreshold() {
				return prof.getTimeThreshold();
			}

			@Override
			public String tag() {
				return prof.getTag();
			}

			@Override
			public boolean normalAndSlowSuffixesEnabled() {
				return prof.isNormalAndSlowSuffixesEnabled();
			}

			@Override
			public String message() {
				return prof.getMessage();
			}

			@Override
			public String logger() {
				return prof.getLogger();
			}

			@Override
			public boolean logFailuresSeparately() {
				return prof.isLogFailureSeparately();
			}

			@Override
			public String level() {
				return prof.getLevel();
			}

			@Override
			public boolean el() {
				return false;
			}
		};
	}

	public MethodMatcherHandler getMethodMatcherHandler() {
		return methodMatcherHandler;
	}

	public void setMethodMatcherHandler(
			MethodMatcherHandler methodMatcherHandler) {
		this.methodMatcherHandler = methodMatcherHandler;
	}

}
