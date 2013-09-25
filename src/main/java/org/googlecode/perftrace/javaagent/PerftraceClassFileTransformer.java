package org.googlecode.perftrace.javaagent;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.googlecode.perftrace.GProfiled;
import org.googlecode.perftrace.RootMethod;
import org.googlecode.perftrace.javassist.CtClass;
import org.googlecode.perftrace.javassist.CtMethod;
import org.googlecode.perftrace.javassist.NotFoundException;
import org.googlecode.perftrace.perf4j.Perf4JLogType;
import org.googlecode.perftrace.schema.BootstrapPerftrace;

/**
 * @author zhongfeng
 * 
 */
public class PerftraceClassFileTransformer extends AbstractClassFileTransformer {

	public final static Logger logger = Logger
			.getLogger(PerftraceClassFileTransformer.class.getName());

	/**
	 * @param bootstrapPerftrace
	 */
	public PerftraceClassFileTransformer(BootstrapPerftrace bootstrapPerftrace) {
		super(bootstrapPerftrace);
	}

	protected String createNewMethodBody(CtMethod ctMethod, CtClass ctClass,
			String orgiName) throws NotFoundException {
		String methodLongName = ctMethod.getLongName();
		GProfiled gProfiled = getGProfiled(ctMethod, ctClass);
		String type = ctMethod.getReturnType().getName();
		boolean isRootMethod = isRootMethod(ctMethod);
		logger.log(Level.INFO, "CtMethod is :{0}, isRootMethod:{1}",
				new Object[] { ctMethod.getLongName(), isRootMethod });
		// 获取LogStopWatch 类型信息
		String logStopWatchClass = Perf4JLogType
				.getLogStopWatchClass(getBootstrapPerftrace()
						.getPerf4JLogType());
		//方法体
		String body = null;
		if ("void".equals(type)) {
			body = "{ \n"
					+ logStopWatchClass
					+ "  stopWatch = new "
					+ logStopWatchClass
					+ "(\""
					+ methodLongName
					+ "\","
					+ isRootMethod
					+ ");\n "
					+ "stopWatch.setNormalAndSlowSuffixesEnabled("
					+ gProfiled.normalAndSlowSuffixesEnabled()
					+ ");"
					+ "stopWatch.setTimeThreshold("
					+ gProfiled.timeThreshold()
					+ "L);"
					+ "try{"
					+ orgiName
					+ "($$); \n"
					+ "}"
					+ "finally{"
					+ " stopWatch.stop(); "
					+ "}" + "}";
			//stopWatch.setMessage(java.util.Arrays.deepToString($args));
		} else {
			body = "{ \n "
					+ type
					+ " result; \n "
					+ logStopWatchClass
					+ "  stopWatch = new "
					+ logStopWatchClass
					+ "(\""
					+ methodLongName
					+ "\","
					+ isRootMethod
					+ ");\n "
					+ "stopWatch.setNormalAndSlowSuffixesEnabled("
					+ gProfiled.normalAndSlowSuffixesEnabled()
					+ ");"
					+ "stopWatch.setTimeThreshold("
					+ gProfiled.timeThreshold()
					+ "L);"
					+ "try{"
					+ "result = "
					+ orgiName
					+ "($$); \n "
					+ "}"
					+ "finally{"
					+ " stopWatch.stop(); \n "
					+ "} \n " + "return result; \n " + "}";
		//	stopWatch.setMessage(java.util.Arrays.deepToString($args));
		}
		return body;
	}

	private boolean isRootMethod(CtMethod ctMethod) {
		if (ctMethod.hasAnnotation(RootMethod.class)) {
			return true;
		}
		return getBootstrapPerftrace().getRootMethodMatcher()
				.isRootClassMethod(ctMethod);
	}

	private GProfiled getGProfiled(CtMethod ctMethod, CtClass cls) {
		/*
		 * GProfiled gProfiled = null; try { gProfiled = (GProfiled)
		 * ctMethod.getAnnotation(GProfiled.class); } catch
		 * (ClassNotFoundException e) { logger.error("error,", e); }
		 */
		// Method m = JavassistHelper.convertToJavaMethod(ctMethod, cls);
		GProfiled configProfiled = getBootstrapPerftrace().getProfileInfoMgr()
				.getProfiled(ctMethod, cls);
		// configProfiled = gProfiled;
		return configProfiled;
	}

}
