package org.googlecode.perftrace.javaagent.model;

import org.googlecode.perftrace.GProfiled;


public class Business implements IBusiness, IBusiness2 {

	@GProfiled
    @Override
    public boolean doSomeThing() {
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("ִdoSomeThing");
        return true;
    }

	@GProfiled
    @Override
    public void doSomeThing2() {
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String s = "doSomeThing2";
        System.out.println(s);
    }
    public static void main(String[] args) {
    	//-javaagent:E:\ota-maven\testAopd\target\testAopd-0.0.1-SNAPSHOT.jar
    	//System.out.println(Business.class.getClassLoader());
        Business h = new Business();
        h.doSomeThing2();
        h.doSomeThing();
    }
}
