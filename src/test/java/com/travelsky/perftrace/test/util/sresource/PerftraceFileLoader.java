package com.travelsky.perftrace.test.util.sresource;

import java.io.File;
import java.io.IOException;

import com.travelsky.perftrace.test.util.sresource.resource.PathMatchingResourcePatternResolver;

public class PerftraceFileLoader {
	
	private final static String PERFTRACE_FILE_NAME = "perftrace.xml";

	private final static PathMatchingResourcePatternResolver RESOLVER = new PathMatchingResourcePatternResolver();
	
	public static File getPerftraceConfigFile() {
	String filePattern = "classpath:" + PERFTRACE_FILE_NAME;
	try {
		return RESOLVER.getResource(filePattern).getFile();
	} catch (IOException e) {
		throw new RuntimeException(e);
	}
}
}
