package org.googlecode.perftrace.aopmatcher;

import java.io.Serializable;

import org.googlecode.perftrace.javassist.CtClass;
import org.googlecode.perftrace.javassist.CtMethod;


/**
 * Canonical MethodMatcher instance that matches all methods.
 * 
 */
@SuppressWarnings("serial")
class TrueMethodMatcher implements MethodMatcher, Serializable {

	public static final TrueMethodMatcher INSTANCE = new TrueMethodMatcher();

	/**
	 * Enforce Singleton pattern.
	 */
	private TrueMethodMatcher() {
	}

	public boolean isRuntime() {
		return false;
	}

	public boolean matches(CtMethod method, CtClass targetClass) {
		return true;
	}

	public boolean matches(CtMethod method, CtClass targetClass, Object[] args) {
		// Should never be invoked as isRuntime returns false.
		throw new UnsupportedOperationException();
	}

	/**
	 * Required to support serialization. Replaces with canonical instance on
	 * deserialization, protecting Singleton pattern. Alternative to overriding
	 * {@code equals()}.
	 */
	private Object readResolve() {
		return INSTANCE;
	}

	@Override
	public String toString() {
		return "MethodMatcher.TRUE";
	}

}
