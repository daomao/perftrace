
package org.googlecode.perftrace.aopmatcher.support;

import java.io.Serializable;

import org.googlecode.perftrace.aopmatcher.ClassFilter;


/**
 * Simple ClassFilter implementation that passes classes (and optionally subclasses)
 * 
 */
@SuppressWarnings("serial")
public class RootClassFilter implements ClassFilter, Serializable {

	private Class clazz;

	// TODO inheritance

	public RootClassFilter(Class clazz) {
		this.clazz = clazz;
	}

	public boolean matches(Class candidate) {
		return clazz.isAssignableFrom(candidate);
	}

}
