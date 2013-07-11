
package org.googlecode.perftrace.aopmatcher.support;

import java.lang.reflect.Method;

import org.googlecode.perftrace.aopmatcher.MethodMatcher;

/**
 * Convenient abstract superclass for dynamic method matchers,
 * which do care about arguments at runtime.
 
 */
public abstract class DynamicMethodMatcher implements MethodMatcher {

	public final boolean isRuntime() {
		return true;
	}

	/**
	 * Can override to add preconditions for dynamic matching. This implementation
	 * always returns true.
	 */
	public boolean matches(Method method, Class<?> targetClass) {
		return true;
	}

}
