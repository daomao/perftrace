package org.googlecode.perftrace.schema.internal;

import org.googlecode.perftrace.DefaultGProfiled;
import org.googlecode.perftrace.GProfiled;
import org.googlecode.perftrace.schema.PerftraceConfig.Global;

/**
 * @author zhongfeng
 * 
 */
public class GlobalSettings {

	private final Global global;

	private final GProfiled defaultGlobalProfiled;

	private static GlobalSettings INST;

	/**
	 * @param global
	 */
	private GlobalSettings(Global global) {
		this.global = global;
		this.defaultGlobalProfiled = DefaultGProfiled.getInstance(global
				.getTimeThreshold());
	}

	public GProfiled getDefaultGlobalProfiled() {
		return defaultGlobalProfiled;
	}

	public Global getGlobal() {
		return global;
	}

	public synchronized static GlobalSettings getInstance(Global global) {
		if (INST == null)
			INST = new GlobalSettings(global);
		return INST;
	}

}
