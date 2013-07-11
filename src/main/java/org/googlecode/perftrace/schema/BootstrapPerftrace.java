package org.googlecode.perftrace.schema;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.googlecode.perftrace.perf4j.Perf4JLogType;
import org.googlecode.perftrace.schema.PerftraceConfig.Global;
import org.googlecode.perftrace.schema.internal.GlobalSettings;
import org.googlecode.perftrace.schema.internal.MethodMatcherHandler;
import org.googlecode.perftrace.schema.internal.ProfileInfoManager;
import org.googlecode.perftrace.schema.internal.RootMethodMatcher;

/**
 * @author zhongfeng
 * 
 */
public class BootstrapPerftrace {

	private final static Logger logger = Logger
			.getLogger(BootstrapPerftrace.class.getName());

	private final ProfileInfoManager profileInfoMgr;

	private final MethodMatcherHandler methodMatcherHandler;

	private final GlobalSettings globalSettings;

	private final RootMethodMatcher rootMethodMatcher;
	
	private final Perf4JLogType perf4JLogType;

	private static BootstrapPerftrace INST;

	/**
	 * 
	 */
	private BootstrapPerftrace(PerftraceConfig perftraceConfig) {
this(perftraceConfig,Perf4JLogType.getDefaultPerf4JLogType());
	}
	
	/**
	 * 
	 */
	private BootstrapPerftrace(PerftraceConfig perftraceConfig, Perf4JLogType type) {
		logger.log(Level.INFO, "Init BootstrapPerftrace");
		this.methodMatcherHandler = MethodMatcherHandlerBuilder
				.createMethodMatcherHandler(perftraceConfig);

		this.globalSettings = GlobalSettings.getInstance(perftraceConfig
				.getGlobal());

		this.profileInfoMgr = ProfiledInfoManagerBuilder
				.buildProfileConfManager(perftraceConfig.getPatternConf(),globalSettings);

		this.rootMethodMatcher = RootMethodMatcher.getInstance(perftraceConfig
				.getGlobal());
		this.perf4JLogType = type;
	}

	public static Global getGlobal() {
		return INST.getGlobalSettings().getGlobal();
	}

	public GlobalSettings getGlobalSettings() {
		return globalSettings;
	}

	public ProfileInfoManager getProfileInfoMgr() {
		return profileInfoMgr;
	}

	public MethodMatcherHandler getMethodMatcherHandler() {
		return methodMatcherHandler;
	}

	public RootMethodMatcher getRootMethodMatcher() {
		return rootMethodMatcher;
	}

	public Perf4JLogType getPerf4JLogType() {
		return perf4JLogType;
	}

	public static BootstrapPerftrace getInstance(PerftraceConfig perftraceCfg, String type) {
		if (INST == null) {
			INST = new BootstrapPerftrace(perftraceCfg,Perf4JLogType.getPerf4JLogType(type));
		}
		return INST;
	}
	
	public static BootstrapPerftrace getInstance(PerftraceConfig perftraceCfg) {
		if (INST == null) {
			INST = new BootstrapPerftrace(perftraceCfg);
		}
		return INST;
	}
}
