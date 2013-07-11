package model;

import org.googlecode.perftrace.GProfiled;

public class BusinessImpl implements IBusiness {

	@GProfiled
	private boolean doSomeThing_Impl() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doSomeThing() {
		return doSomeThing_Impl();
	}

	
}
