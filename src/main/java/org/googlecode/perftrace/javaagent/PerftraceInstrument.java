package org.googlecode.perftrace.javaagent;

import java.lang.instrument.Instrumentation;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.googlecode.perftrace.schema.BootstrapPerftrace;
import org.googlecode.perftrace.schema.PerftraceConfig;
import org.googlecode.perftrace.schema.PerftraceConfigBuilder;
import org.googlecode.perftrace.util.StringUtils;

/**
 * @author zhongfeng
 * 
 */
public class PerftraceInstrument {

	private final static Logger logger = Logger
			.getLogger(PerftraceInstrument.class.getName());

	/**
	 * @poptions
	 * @param ins
	 */
	public static void premain(String options, Instrumentation ins) {

		action(options, ins, false);
	}

	/**
	 * 
	 * 目前使用的javassist，修改了方法签名，增加了新的方法，因此在目前的版本里 The retransformation must not
	 * add, remove or rename fields or methods, change the signatures of
	 * methods, or change inheritance.
	 * 
	 * @param agentArgs
	 * @param inst
	 * 
	 * 
	 */
	public static void agentmain(String agentArgs, Instrumentation inst) {
		action(agentArgs, inst, true);
		/*
		 * * The retransformation may change method bodies, the constant pool
		 * and attributes. The retransformation must not add, remove or rename
		 * fields or methods, change the signatures of methods, or change
		 * inheritance. These restrictions maybe be lifted in future versions.
		 * The class file bytes are not checked, verified and installed until
		 * after the transformations have been applied, if the resultant bytes
		 * are in error this method will throw an exception.*
		 */
		// try {
		// if( inst.isRetransformClassesSupported())
		// {
		// logger.info("agent main redefine class");
		// for (Class<?> cls : inst.getAllLoadedClasses()) {
		// if(cls.getName().contains("Business"))
		// inst.retransformClasses(cls);
		// }
		// }
		// } catch (UnmodifiableClassException e) {
		// logger.error("e", e);
		// }
	}

	/**
	 * @param options
	 * @param ins
	 */
	private static void action(String options, Instrumentation ins,
			boolean canRetransform) {
		logger.log(Level.INFO, "options is :{0}", options);
		//格式为perftrace.xml绝对路径;日志类型
		String[] opTmp = StringUtils.split(options, ";");
		String perftraceFileName = null;
		String logType = "jdklog";
		if (opTmp != null && opTmp.length == 2) {
			perftraceFileName = opTmp[0];
			logType = opTmp[1];
		}
		
		PerftraceConfig pfCfg = PerftraceConfigBuilder
				.getPerftraceConfig(perftraceFileName);
		BootstrapPerftrace bootstrapPerftrace = BootstrapPerftrace.getInstance(
				pfCfg, logType);
		ins.addTransformer(
				new PerftraceClassFileTransformer(bootstrapPerftrace),
				canRetransform);
	}
}
