package org.googlecode.perftrace.aopmatcher.support.annotation;

import java.lang.annotation.Annotation;


import org.googlecode.perftrace.aopmatcher.support.StaticMethodMatcher;
import org.googlecode.perftrace.javassist.CtClass;
import org.googlecode.perftrace.javassist.CtMethod;

/**
 * Simple MethodMatcher that looks for a specific Java 5 annotation being
 * present on a method (checking both the method on the invoked interface, if
 * any, and the corresponding method on the target class).
 * 
 * @see AnnotationMatchingPointcut
 */
public class AnnotationMethodMatcher extends StaticMethodMatcher {

	private final Class<? extends Annotation> annotationType;

	
	/**
	 * Create a new AnnotationClassFilter for the given annotation type.
	 * 
	 * @param annotationType
	 *            the annotation type to look for
	 */
	public AnnotationMethodMatcher(Class<? extends Annotation> annotationType) {
		// Assert.notNull(annotationType, "Annotation type must not be null");
		this.annotationType = annotationType;
	}

	public boolean matches(CtMethod method, CtClass targetClass) {
		if (method.hasAnnotation(this.annotationType)) {
			return true;
		}
		// The method may be on an interface, so let's check on the target class
		// as well.
		// Method specificMethod = AopUtils.getMostSpecificMethod(method,
		// targetClass);
		// return (specificMethod != method &&
		// specificMethod.isAnnotationPresent(this.annotationType));
		return false;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AnnotationMethodMatcher)) {
			return false;
		}
		AnnotationMethodMatcher otherMm = (AnnotationMethodMatcher) other;
		return this.annotationType.equals(otherMm.annotationType);
	}

	@Override
	public int hashCode() {
		return this.annotationType.hashCode();
	}

}
