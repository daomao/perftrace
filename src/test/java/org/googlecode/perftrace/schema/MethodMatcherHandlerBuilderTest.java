/**
 * 
 */
package org.googlecode.perftrace.schema;

import java.lang.reflect.Method;

import org.googlecode.perftrace.GProfiled;
import org.googlecode.perftrace.schema.internal.MethodMatcherHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.travelsky.perftrace.test.util.sresource.PerftraceFileLoader;

/**
 * @author zhongfeng
 * 
 */
public class MethodMatcherHandlerBuilderTest {

	private MethodMatcherHandler handler;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		PerftraceConfig pfCfg = PerftraceConfigBuilder.getPerftraceConfig(PerftraceFileLoader.getPerftraceConfigFile().getAbsolutePath());

		handler = MethodMatcherHandlerBuilder.createMethodMatcherHandler(pfCfg);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link org.googlecode.perftrace.schema.builder.MethodMatcherHandlerBuilder#createMethodMatcherHandler(org.googlecode.perftrace.schema.PerftraceConfig)}
	 * .
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 */
	@Test
	public final void testCreateMethodMatcherHandler() throws SecurityException, NoSuchMethodException {
		Method tMethod = Test1.class.getMethod("test");
		//assertEquals(handler.matches(tMethod, Test1.class),true);
	}

	@Test
	public final void testCreateMethodMatcherHandler1() throws SecurityException, NoSuchMethodException {
		Method tMethod = Test1.class.getMethod("test2");
		//assertEquals(handler.matches(tMethod, Test1.class),true);
	}
	
	public static class Test1 {
		@GProfiled
		public void test() {
		}
		
		public void test2(){}
		
		public void test3(){}
	}

}
