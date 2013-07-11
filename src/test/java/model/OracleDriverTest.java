package model;


import java.sql.Statement;
import java.sql.PreparedStatement;

import org.googlecode.perftrace.javassist.ClassPool;
import org.googlecode.perftrace.javassist.CtClass;
import org.googlecode.perftrace.javassist.CtMethod;
import org.googlecode.perftrace.javassist.NotFoundException;
import org.junit.Ignore;

@Ignore
public class OracleDriverTest {

	public static boolean isJDBCRawMatch(CtClass ctCls) throws NotFoundException {
		if (ctCls == null) {
			return false;
		}
		if (hasImplementedJdbcInterface(ctCls)) {
			return true;
		}
		for (CtClass c : ctCls.getInterfaces()) {
			return isJDBCRawMatch(c);
		}
		return isJDBCRawMatch(ctCls.getSuperclass());
	}

	/**
	 * @param ctCls
	 * @return
	 * @throws NotFoundException
	 */
	private static boolean hasImplementedJdbcInterface(CtClass ctCls)
			throws NotFoundException {
		if (isJDBCStatement(ctCls)) {
			return true;
		}
		for (CtClass c : ctCls.getInterfaces()) {
			if (isJDBCStatement(c)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isJDBCStatement(CtClass ctClass) {
		if (ctClass.getName().equals("java.sql.Statement")
				|| ctClass.getName().equals("java.sql.PreparedStatement"))
			return true;
		return false;
	}

	public static void main(String[] args) throws Exception {
		ClassPool pool = ClassPool.getDefault();
		CtClass ctCls = pool.get("oracle.jdbc.driver.T4CStatement");
		for(CtMethod ctMethod:ctCls.getMethods())
		{
			System.out.println(ctMethod.getName());
		}
		// for (CtClass c : ctCls.getSuperclass()) {
		System.out.println(isJDBCRawMatch(ctCls));
		System.out.println(isJDBCRawMatch(pool.get("java.lang.Object")));
		System.out.println(isJDBCRawMatch(pool.get("java.sql.Statement")));
		// }
	}
}
