package model;

import org.googlecode.perftrace.GProfiled;
import org.googlecode.perftrace.RootMethod;


public class Business extends BusinessImpl implements IBusiness2 {
	
	@Override
	public boolean doSomeThing() {
		return doSomeThing_Impl();
	}
	
	//@GProfiled
    private boolean doSomeThing_Impl() {
		super.doSomeThing();
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
        System.out.println("ִdoSomeThing");
        //throw new RuntimeException("test");
        return true;
    }

	//@GProfiled
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
	@RootMethod
    public static void main(String[] args) {
		//-javaagent:F:\git_project\perftrace\target\perftrace-2.2.0.jar[=F:\git_project\perftrace\src\test\resources\perftrace.xml;slf4j]
    	//-javaagent:E:\ota-maven\testAopd\target\testAopd-0.0.1-SNAPSHOT.jar
    	//System.out.println(Business.class.getClassLoader());
        Business h = new Business();
        try {    
        	for(int i =0;i<3;i++)
        	{
        		Thread.sleep(1000);
			h.doSomeThing2();
			h.doSomeThing();
        	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
