package model;

import org.googlecode.perftrace.GProfiled;

/**
 *
 * @author tengfei.fangtf
 *
 */
public interface IBusiness {
    
	@GProfiled
    public boolean doSomeThing();

}
