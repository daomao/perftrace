package org.googlecode.perftrace.schema;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.googlecode.perftrace.aopmatcher.support.JdkRegexpMethodMatcher;
import org.googlecode.perftrace.aopmatcher.support.NameMatchMethodMatcher;
import org.googlecode.perftrace.aopmatcher.support.annotation.AnnotationMethodMatcher;
import org.googlecode.perftrace.schema.PatternType;
import org.googlecode.perftrace.schema.PerftraceConfig.PatternConf;
import org.googlecode.perftrace.schema.PerftraceConfig.PatternConf.Pelement;
import org.googlecode.perftrace.schema.PerftraceConfig.PatternConf.Pelement.Patattr;
import org.googlecode.perftrace.schema.PerftraceConfig.PatternConf.Pelement.Profiled;
import org.googlecode.perftrace.schema.internal.GlobalSettings;
import org.googlecode.perftrace.schema.internal.ProfileInfoManager;
import org.googlecode.perftrace.schema.internal.ProfiledHandler;
import org.googlecode.perftrace.util.StringUtils;

/**
 * @author zhongfeng
 * 
 */
public abstract class ProfiledInfoManagerBuilder {

	/**
	 * @return
	 */
	public static ProfileInfoManager buildProfileConfManager(
			PatternConf patternConf, GlobalSettings globalSettings) {
		List<ProfiledHandler> handlers = new ArrayList<ProfiledHandler>();
		for (Pelement pelement : patternConf.getPelement()) {
			handlers.add(ProfiledHandlerBuilder.buildProfiledHandler(pelement));
		}
		return new ProfileInfoManager(handlers, globalSettings
				.getDefaultGlobalProfiled());
	}

	/**
	 * @author zhongfeng
	 * 
	 */
	public static class ProfiledHandlerBuilder {

		public static ProfiledHandler buildProfiledHandler(Pelement pelement) {
			ProfiledHandler handler = new ProfiledHandler();
			for (Patattr pattern : pelement.getPatattr()) {
				if (pattern.getType().equals(PatternType.ANNOTATION)) {
					handler.getMethodMatcherHandler().addMethodMatcher(
							buildAnnotationMethodMatcher(pattern));
				}
				if (pattern.getType().equals(PatternType.NAME)) {
					handler.getMethodMatcherHandler().addMethodMatcher(
							buildNameMatchMethod(pattern));
				}
				if (pattern.getType().equals(PatternType.REGEX)) {
					handler.getMethodMatcherHandler().addMethodMatcher(
							buildJdkRegexMatchMethod(pattern));
				}
			}
			Profiled prof = pelement.getProfiled();
			handler.setProfiled(prof);
			return handler;
		}

		/**
		 * @param pattern
		 * @return
		 */
		private static JdkRegexpMethodMatcher buildJdkRegexMatchMethod(
				Patattr pattern) {
			JdkRegexpMethodMatcher jdkRegexMethodPointcut = new JdkRegexpMethodMatcher();
			for (String p : StringUtils.split(pattern.getValue())) {
				if (!StringUtils.isBlank(p))
					jdkRegexMethodPointcut.setPattern(p);
			}
			return jdkRegexMethodPointcut;
		}

		/**
		 * @param pattern
		 * @return
		 */
		private static NameMatchMethodMatcher buildNameMatchMethod(
				Patattr pattern) {
			NameMatchMethodMatcher nameMatchMethodMatcher = new NameMatchMethodMatcher();
			for (String mappedName : StringUtils.split(pattern.getValue())) {
				if (!StringUtils.isBlank(mappedName))
					nameMatchMethodMatcher.addMethodName(mappedName);
			}
			return nameMatchMethodMatcher;
		}

		/**
		 * @param pattern
		 * @return
		 */
		@SuppressWarnings("unchecked")
		private static AnnotationMethodMatcher buildAnnotationMethodMatcher(
				Patattr pattern) {
			Class<? extends Annotation> cls = null;
			try {
				cls = (Class<? extends Annotation>) Class.forName(StringUtils
						.strip(pattern.getValue()));
			} catch (ClassNotFoundException e) {
				// logger.error("build AnnotationMatchingPointcut error.",
				// e);
				throw new RuntimeException(e);
			}
			AnnotationMethodMatcher methodMatcher = new AnnotationMethodMatcher(
					cls);
			return methodMatcher;
		}
	}

}
