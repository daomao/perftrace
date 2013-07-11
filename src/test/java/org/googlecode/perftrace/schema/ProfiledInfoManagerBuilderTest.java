package org.googlecode.perftrace.schema;

import java.lang.reflect.Method;

import org.googlecode.perftrace.GProfiled;
import org.googlecode.perftrace.schema.internal.ProfileInfoManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.travelsky.perftrace.test.util.sresource.PerftraceFileLoader;

public class ProfiledInfoManagerBuilderTest {

	private ProfileInfoManager proinfoMgr;

	@Before
	public void setUp() throws Exception {
		PerftraceConfig pfCfg = PerftraceConfigBuilder.getPerftraceConfig(PerftraceFileLoader.getPerftraceConfigFile().getAbsolutePath());
		proinfoMgr = BootstrapPerftrace.getInstance(pfCfg).getProfileInfoMgr();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testBuildProfileConfManager() throws SecurityException,
			NoSuchMethodException {
		Method tMethod = Test1.class.getMethod("test2");
		// assertEquals(proinfoMgr.getProfiled(tMethod,
		// Test1.class).timeThreshold(),10L);
		Method tMethod1 = Test1.class.getMethod("test");
		// assertEquals(proinfoMgr.getProfiled(tMethod1,
		// Test1.class).timeThreshold(),1000L);
	}

	public static class Test1 {
		@GProfiled
		public void test() {
		}

		public void test2() {
		}

		public void test3() {
		}
	}

}
