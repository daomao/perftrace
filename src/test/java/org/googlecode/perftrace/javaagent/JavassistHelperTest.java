package org.googlecode.perftrace.javaagent;


import org.googlecode.perftrace.javassist.ClassPool;
import org.googlecode.perftrace.javassist.CtClass;
import org.googlecode.perftrace.javassist.CtMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class JavassistHelperTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Ignore
	@Test
	public void testConvertToJavaMethod() throws Exception {
		ClassPool cp = ClassPool.getDefault();

		CtClass cc = cp
				.get("org.googlecode.perftrace.javaagent.model.Business");
		CtMethod m = cc.getDeclaredMethod("doSomeThing");
	}

}
