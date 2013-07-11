package org.googlecode.perftrace.javaagent;


import org.googlecode.perftrace.javassist.ClassPool;
import org.googlecode.perftrace.javassist.CtClass;
import org.googlecode.perftrace.javassist.CtMethod;
import org.googlecode.perftrace.schema.BootstrapPerftrace;
import org.googlecode.perftrace.schema.PerftraceConfig;
import org.googlecode.perftrace.schema.PerftraceConfigBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.travelsky.perftrace.test.util.sresource.PerftraceFileLoader;

public class Perf4JClassFileTransformerTest {

	private PerftraceClassFileTransformer t ;

	@Before
	public void setUp() throws Exception {
		
		PerftraceConfig pfCfg = PerftraceConfigBuilder.getPerftraceConfig(PerftraceFileLoader.getPerftraceConfigFile().getAbsolutePath());
		BootstrapPerftrace bootstrapPerftrace = BootstrapPerftrace.getInstance(pfCfg);
		t = new PerftraceClassFileTransformer(bootstrapPerftrace);
	}

	@After
	public void tearDown() throws Exception {
	}

	//@Ignore
	@Test
	public void testDoMethod() throws Exception {
		ClassPool cp = ClassPool.getDefault();

		CtClass cc = cp
				.get("java.lang.String");
		CtMethod m = cc.getDeclaredMethod("toString");
		t.doMethod(m, cc ,null);
	}


}
