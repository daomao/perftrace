
package org.googlecode.perftrace.aopmatcher.support;


import org.googlecode.perftrace.aopmatcher.MethodMatcher;
import org.googlecode.perftrace.javassist.CtClass;
import org.googlecode.perftrace.javassist.CtMethod;

/**
 * Convenient abstract superclass for static method matchers, which don't care
 * about arguments at runtime.
 */
public abstract class StaticMethodMatcher implements MethodMatcher {

	public final boolean isRuntime() {
		return false;
	}

	public final boolean matches(CtMethod method, CtClass targetClass, Object[] args) {
		// should never be invoked because isRuntime() returns false
		throw new UnsupportedOperationException("Illegal MethodMatcher usage");
	}

}
