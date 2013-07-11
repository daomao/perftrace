package org.googlecode.perftrace.schema.internal;

import static org.junit.Assert.assertEquals;

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

public class RootMethodMatcherTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIsRootClassMethod() throws Exception {
		
		PerftraceConfig pfCfg = PerftraceConfigBuilder.getPerftraceConfig(PerftraceFileLoader.getPerftraceConfigFile().getAbsolutePath());
		RootMethodMatcher rmm = BootstrapPerftrace.getInstance(pfCfg).getRootMethodMatcher();
		ClassPool cp = ClassPool.getDefault();

		CtClass cc = cp
				.get("model.Business");
		CtMethod m = cc.getDeclaredMethod("main");
		assertEquals(rmm.isRootClassMethod(m),true);
	}

	public static void main(String[] args)
	{
		
	}
}
