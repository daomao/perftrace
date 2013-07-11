/**
 * 
 */
package org.googlecode.perftrace.schema;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.travelsky.perftrace.test.util.sresource.PerftraceFileLoader;

/**
 * @author zhongfeng
 * 
 */
public class InitPerftraceConfigTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		PerftraceConfig pfCfg = PerftraceConfigBuilder.getPerftraceConfig(PerftraceFileLoader.getPerftraceConfigFile().getAbsolutePath());
		BootstrapPerftrace bp  = BootstrapPerftrace.getInstance(pfCfg);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link org.googlecode.perftrace.schema.BootstrapPerftrace#getPerftraceConfig()}
	 * .
	 */
	@Test
	public void testGetPerftraceConfig() {
	}
}
