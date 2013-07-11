package org.googlecode.perftrace.javaagent.util;

import org.googlecode.perftrace.javassist.CtBehavior;
import org.googlecode.perftrace.javassist.CtClass;
import org.googlecode.perftrace.javassist.CtConstructor;
import org.googlecode.perftrace.javassist.CtMethod;
import org.googlecode.perftrace.javassist.Modifier;
import org.googlecode.perftrace.javassist.NotFoundException;
import org.googlecode.perftrace.javassist.bytecode.CodeAttribute;
import org.googlecode.perftrace.javassist.bytecode.LocalVariableAttribute;

/**
 * @author zhongfeng
 * 
 */
public class JavassistHelper {

	/**
	 * @param method
	 * @return
	 * @throws NotFoundException
	 */
	public static String returnValue(CtBehavior method)
			throws NotFoundException {
		String returnValue = "";
		if (methodReturnsValue(method)) {
			returnValue = "\" returns: \" + $_ ";
		}
		return returnValue;
	}

	/**
	 * @param method
	 * @return
	 * @throws NotFoundException
	 */
	public static boolean methodReturnsValue(CtBehavior method)
			throws NotFoundException {
		CtClass returnType = ((CtMethod) method).getReturnType();
		String returnTypeName = returnType.getName();

		boolean isVoidMethod = (method instanceof CtMethod)
				&& "void".equals(returnTypeName);
		boolean isConstructor = method instanceof CtConstructor;

		boolean methodReturnsValue = (isVoidMethod || isConstructor) == false;
		return methodReturnsValue;
	}

	/**
	 * @param method
	 * @return
	 * @throws NotFoundException
	 */
	public static String getSignature(CtBehavior method)
			throws NotFoundException {
		CtClass parameterTypes[] = method.getParameterTypes();

		CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();

		LocalVariableAttribute locals = (LocalVariableAttribute) codeAttribute
				.getAttribute("LocalVariableTable");
		String methodName = method.getName();

		StringBuffer sb = new StringBuffer(methodName + "(\" ");
		for (int i = 0; i < parameterTypes.length; i++) {
			if (i > 0) {
				sb.append(" + \", \" ");
			}

			CtClass parameterType = parameterTypes[i];
			CtClass arrayOf = parameterType.getComponentType();

			sb.append(" + \"");
			sb.append(parameterNameFor(method, locals, i));
			sb.append("\" + \"=");

			// use Arrays.asList() to render array of objects.
			if (arrayOf != null && !arrayOf.isPrimitive()) {
				sb.append("\"+ java.util.Arrays.asList($" + (i + 1) + ")");
			} else {
				sb.append("\"+ $" + (i + 1));
			}
		}
		sb.append("+\")\"");

		String signature = sb.toString();
		return signature;
	}

	/**
	 * @param method
	 * @param locals
	 * @param i
	 * @return
	 */
	public static String parameterNameFor(CtBehavior method,
			LocalVariableAttribute locals, int i) {
		if (locals == null) {
			return Integer.toString(i + 1);
		}

		if (Modifier.isStatic(method.getModifiers())) {
			return locals.variableName(i);
		}

		// skip #0 which is reference to "this"
		return locals.variableName(i + 1);
	}

}
