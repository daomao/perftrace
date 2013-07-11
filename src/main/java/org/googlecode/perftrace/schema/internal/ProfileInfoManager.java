package org.googlecode.perftrace.schema.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.googlecode.perftrace.GProfiled;
import org.googlecode.perftrace.javassist.CtClass;
import org.googlecode.perftrace.javassist.CtMethod;

/**
 * @author zhongfeng
 * 
 */
public class ProfileInfoManager {

	private final static Logger logger = Logger
			.getLogger(ProfileInfoManager.class.getName());

	private List<ProfiledHandler> handlers = new ArrayList<ProfiledHandler>();
	private final GProfiled defaultGlobalProfiled;

	/**
	 * @param handlers
	 */
	public ProfileInfoManager(List<ProfiledHandler> handlers,
			GProfiled defaultGlobalProfiled) {
		this.handlers = handlers;
		this.defaultGlobalProfiled = defaultGlobalProfiled;
	}

	public GProfiled getProfiled(CtMethod method, CtClass targetClass) {
		for (ProfiledHandler handler : getHandlers()) {
			if (handler.getMethodMatcherHandler().matches(method, targetClass))
				logger.log(Level.INFO ,"------", handler.getProfiled().timeThreshold());
			return handler.getProfiled();
		}
		logger.info("------ default");
		return getDefaultGProfiled();
	}

	private GProfiled getDefaultGProfiled() {
		return defaultGlobalProfiled;
	}

	public List<ProfiledHandler> getHandlers() {
		return handlers;
	}

	public void setHandlers(List<ProfiledHandler> handlers) {
		this.handlers = handlers;
	}
}
