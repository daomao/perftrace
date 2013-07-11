package org.googlecode.perftrace.schema.internal;

import java.util.ArrayList;
import java.util.List;


import org.googlecode.perftrace.GProfiled;
import org.googlecode.perftrace.aopmatcher.MethodMatcher;
import org.googlecode.perftrace.aopmatcher.support.annotation.AnnotationMethodMatcher;
import org.googlecode.perftrace.javassist.CtClass;
import org.googlecode.perftrace.javassist.CtMethod;

/**
 * @author zhongfeng
 * 
 */
public class MethodMatcherHandler {

	private final List<MethodMatcher> matchers = new ArrayList<MethodMatcher>(1);

	public MethodMatcherHandler() {
		//默认打上GProfile annotation的，都需要记录性能
		addMethodMatcher(new AnnotationMethodMatcher(GProfiled.class));
	}

	public void addMethodMatcher(MethodMatcher methodMatcher) {
		if (methodMatcher != null)
			matchers.add(methodMatcher);
	}

	public boolean matches(CtMethod method, CtClass targetClass) {
		for (MethodMatcher mm : matchers) {
			if (mm.matches(method, targetClass))
				return true;
		}
		return false;
	}

}
