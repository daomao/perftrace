package org.googlecode.perftrace.schema;

import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.googlecode.perftrace.aopmatcher.support.JdkRegexpMethodMatcher;
import org.googlecode.perftrace.aopmatcher.support.NameMatchMethodMatcher;
import org.googlecode.perftrace.aopmatcher.support.annotation.AnnotationMethodMatcher;
import org.googlecode.perftrace.schema.PerftraceConfig.Matcher;
import org.googlecode.perftrace.schema.PerftraceConfig.Matcher.AnnotationMatcher;
import org.googlecode.perftrace.schema.PerftraceConfig.Matcher.NameMatcher;
import org.googlecode.perftrace.schema.PerftraceConfig.Matcher.RegexMatcher;
import org.googlecode.perftrace.schema.internal.MethodMatcherHandler;
import org.googlecode.perftrace.util.StringUtils;

/**
 * @author zhongfeng
 * 
 */
public abstract class MethodMatcherHandlerBuilder {

	private final static Logger logger = Logger
			.getLogger(MethodMatcherHandlerBuilder.class.getName());

	public static MethodMatcherHandler createMethodMatcherHandler(
			PerftraceConfig config) {
		Matcher m = config.getMatcher();
		MethodMatcherHandler handler = new MethodMatcherHandler();
		if (m != null) {
			handler.addMethodMatcher(buildAnnotationMatchingPointcut(m));
			handler.addMethodMatcher(buildNameMatchMethodPointcut(m));
			handler.addMethodMatcher(buildJdkRegexMethodPointcut(m));
		}
		return handler;
	}

	/**
	 * @param m
	 */
	private static NameMatchMethodMatcher buildNameMatchMethodPointcut(Matcher m) {
		NameMatcher nameMatcher = m.getNameMatcher();
		NameMatchMethodMatcher nameMatchMethodMatcher = null;
		if (nameMatcher != null) {
			nameMatchMethodMatcher = new NameMatchMethodMatcher();
			for (String mappedName : StringUtils.split(nameMatcher
					.getMappedNames())) {
				if (!StringUtils.isBlank(mappedName))
					nameMatchMethodMatcher.addMethodName(mappedName);
			}
		}
		return nameMatchMethodMatcher;
	}

	/**
	 * @param m
	 */
	private static JdkRegexpMethodMatcher buildJdkRegexMethodPointcut(Matcher m) {
		RegexMatcher regexMatcher = m.getRegexMatcher();
		JdkRegexpMethodMatcher jdkRegexMethodPointcut = null;
		if (regexMatcher != null) {
			jdkRegexMethodPointcut = new JdkRegexpMethodMatcher();
			for (String pattern : StringUtils.split(regexMatcher.getPatterns()))
				if (!StringUtils.isBlank(pattern))
					jdkRegexMethodPointcut.setPattern(pattern);
			for (String excludePattern : StringUtils.split(regexMatcher
					.getExcludePatterns() == null ? "" : regexMatcher
					.getExcludePatterns()))
				if (!StringUtils.isBlank(excludePattern))
					jdkRegexMethodPointcut.setExcludedPattern(excludePattern);
		}
		return jdkRegexMethodPointcut;
	}

	/**
	 * @param m
	 */
	@SuppressWarnings("unchecked")
	private static AnnotationMethodMatcher buildAnnotationMatchingPointcut(
			Matcher m) {
		AnnotationMethodMatcher amp = null;
		AnnotationMatcher am = m.getAnnotationMatcher();
		if (am != null) {
			Class<? extends Annotation> cls = null;
			try {
				cls = (Class<? extends Annotation>) Class.forName(StringUtils
						.strip(am.getAnnotationType()));
			} catch (ClassNotFoundException e) {
				logger.log(Level.SEVERE ,"build AnnotationMatchingPointcut error.", e);
				throw new RuntimeException(e);
			}
			amp = new AnnotationMethodMatcher(cls);
		}
		return amp;
	}
}