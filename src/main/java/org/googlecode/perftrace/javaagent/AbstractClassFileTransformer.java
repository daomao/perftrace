package org.googlecode.perftrace.javaagent;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.googlecode.perftrace.javaagent.util.ClassFactoryClassPool;
import org.googlecode.perftrace.javassist.CannotCompileException;
import org.googlecode.perftrace.javassist.ClassPool;
import org.googlecode.perftrace.javassist.CtClass;
import org.googlecode.perftrace.javassist.CtMethod;
import org.googlecode.perftrace.javassist.CtNewMethod;
import org.googlecode.perftrace.javassist.NotFoundException;
import org.googlecode.perftrace.schema.BootstrapPerftrace;
import org.googlecode.perftrace.util.StringUtils;

/**
 * @author zhongfeng
 * 
 */
public abstract class AbstractClassFileTransformer implements
		ClassFileTransformer {

	private final static Logger logger = Logger
			.getLogger(AbstractClassFileTransformer.class.getName());

	private BootstrapPerftrace bootstrapPerftrace;

	/**
	 * @param bootstrapPerftrace
	 */
	public AbstractClassFileTransformer(BootstrapPerftrace bootstrapPerftrace) {
		this.bootstrapPerftrace = bootstrapPerftrace;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		if (className != null && className.indexOf("/") != -1) {
			className = className.replaceAll("/", ".");
		}//
		// ||
		if (StringUtils.startsWith(className, "org.googlecode.perftrace.")
				|| StringUtils.startsWith(className, "java.")
				|| StringUtils.startsWith(className, "javax.")
				|| StringUtils.startsWith(className, "sun.")
				|| StringUtils.startsWith(className, "com.sun."))
			return classfileBuffer;
		return doClass(loader, className, classBeingRedefined, classfileBuffer);
	}

	protected byte[] doClass(ClassLoader loader, String className,
			Class<?> clazz, byte[] b) {
		// add loaderclasspath
		ClassPool pool = new ClassFactoryClassPool(loader);
		logger.log(Level.INFO, "RawClassLoader:" + loader.getClass().getName()
				+ "-ClassName:" + className + "-PoolClassLoader:"
				+ pool.getClassLoader());
		CtClass ctClass = null;
		byte[] ret = null;
		try {

			ctClass = pool.makeClass(new java.io.ByteArrayInputStream(b));
			if (ctClass.isInterface() == false) {
				CtMethod[] methods = ctClass.getDeclaredMethods();
				for (int i = 0; i < methods.length; i++) {
					if (isMatcher(methods[i], ctClass)) {
						doMethod(methods[i], ctClass, clazz);
					}
				}
				ret = ctClass.toBytecode();
			}
		} catch (Exception e) {
			if (logger.isLoggable(Level.SEVERE)) {
				logger.log(Level.SEVERE, "Could not instrument className:"
						+ className, e);
			}
			ret = b;
		} finally {
			if (ctClass != null) {
				ctClass.detach();
			}
		}
		return ret;
	}

	/**
	 * @param loader
	 * @param pool
	 * @throws IOException
	 * @throws NotFoundException
	 */
	protected void addClassPath(ClassLoader loader, ClassPool pool) {
	}

	protected void doMethod(CtMethod ctMethod, CtClass ctClass,
			Class<?> javaClazz) throws NotFoundException,
			CannotCompileException {
		logger.log(Level.FINE, "TransForm MethodName:{},ClassName:{}",
				new Object[] { ctMethod.getName(), ctClass.getName() });
		String mname = ctMethod.getName();
		String rename = buildMethodName(mname, ctClass);
		ctMethod.setName(rename);
		CtMethod mnew = CtNewMethod.copy(ctMethod, mname, ctClass, null);
		// build body text of method to wrap call with perftrace
		String body = createNewMethodBody(mnew, ctClass, rename);
		mnew.setBody(body);
		ctClass.addMethod(mnew);
	}

	// 解决public boolean doSomeThing() { super.doSomeThing() …… …… ;
	private String buildMethodName(String methodName, CtClass ctClass) {
		String[] nameSegs = StringUtils.split(ctClass.getName(), ".");
		String classShortName = nameSegs[nameSegs.length - 1];
		return methodName + "$impl" + "_" + classShortName;
	}

	/**
	 * @param method
	 * @param javaClazz
	 * @param mname
	 * @return
	 * @throws NotFoundException
	 */
	protected abstract String createNewMethodBody(CtMethod ctMethod,
			CtClass ctClass, String orgiName) throws NotFoundException;

	private boolean isMatcher(CtMethod ctMethod, CtClass ctClass) {
		if (ctMethod.isEmpty()) {
			return false;
		}
		return getBootstrapPerftrace().getMethodMatcherHandler().matches(
				ctMethod, ctClass);
	}

	public BootstrapPerftrace getBootstrapPerftrace() {
		return bootstrapPerftrace;
	}

	public void setBootstrapPerftrace(BootstrapPerftrace bootstrapPerftrace) {
		this.bootstrapPerftrace = bootstrapPerftrace;
	}
}
