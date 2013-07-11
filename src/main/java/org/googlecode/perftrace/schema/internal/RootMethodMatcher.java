package org.googlecode.perftrace.schema.internal;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.googlecode.perftrace.javassist.CtClass;
import org.googlecode.perftrace.javassist.CtMethod;
import org.googlecode.perftrace.javassist.NotFoundException;
import org.googlecode.perftrace.schema.PerftraceConfig.Global;
import org.googlecode.perftrace.schema.PerftraceConfig.Global.RootMethods;
import org.googlecode.perftrace.schema.PerftraceConfig.Global.RootMethods.RootMethod;
import org.googlecode.perftrace.schema.PerftraceConfig.Global.RootMethods.RootMethod.ParametersType;
import org.googlecode.perftrace.schema.PerftraceConfig.Global.RootMethods.RootMethod.ParametersType.Type;
import org.googlecode.perftrace.util.StringUtils;

/**
 * @author zhongfeng
 * 
 */
public class RootMethodMatcher {

	private final static Logger logger = Logger
			.getLogger(RootMethodMatcher.class.getName());

	private Global global;

	/**
	 * @param global
	 */
	private RootMethodMatcher(Global global) {
		this.global = global;
	}

	public boolean isRootClassMethod(CtMethod ctMethod) {
		RootMethods rootMethods = global.getRootMethods();
		if (rootMethods == null) {
			return false;
		}
		for (RootMethod rootMethod : rootMethods.getRootMethod()) {
			if (compareMethods(rootMethod, ctMethod)) {
				return true;
			}
		}
		return false;
	}

	private boolean compareMethods(RootMethod rootMethod, CtMethod ctMethod) {
		if (!StringUtils.equalsIgnoreCase(removeParameterTypeString(ctMethod),
				StringUtils.strip(rootMethod.getMethodName()))) {
			return false;
		}
		String[] m = buildCtMethodParameterTypes(ctMethod);
		String[] mt = buildMethodParametersConfig(rootMethod);
		return Arrays.deepEquals(m, mt);
	}

	/**
	 * @param ctMethod
	 * @return
	 */
	private String removeParameterTypeString(CtMethod ctMethod) {
		return ctMethod.getLongName().replaceAll("\\(.*\\)", "");
	}

	/**
	 * @param rootMethod
	 * @return
	 */
	private String[] buildMethodParametersConfig(RootMethod rootMethod) {
		ParametersType pt = rootMethod.getParametersType();
		String[] mt = new String[0];
		if (pt != null) {
			List<Type> typeList = pt.getType();
			mt = new String[typeList.size()];
			for (int i = 0; i < typeList.size(); i++) {
				mt[i] = StringUtils.strip(typeList.get(i).getClazz());
			}
		}
		return mt;
	}

	/**
	 * @param ctMethod
	 * @return
	 */
	private String[] buildCtMethodParameterTypes(CtMethod ctMethod) {
		CtClass[] paramTypes = null;
		String[] m = new String[0];
		try {
			paramTypes = ctMethod.getParameterTypes();
		} catch (NotFoundException e) {
			logger.log(Level.SEVERE, "", e);
		}
		if (paramTypes != null) {
			m = new String[paramTypes.length];
			for (int i = 0; i < paramTypes.length; i++) {
				m[i] = paramTypes[i].getName();
			}
		}
		return m;
	}

	public static RootMethodMatcher getInstance(Global global) {
		return new RootMethodMatcher(global);
	}

}
