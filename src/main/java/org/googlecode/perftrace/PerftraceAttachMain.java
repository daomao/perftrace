package org.googlecode.perftrace;

import java.io.IOException;
import com.sun.tools.attach.VirtualMachine;

/**
 * @author zhongfeng
 * 
 */
public class PerftraceAttachMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			throw new IllegalArgumentException("Args num should be 3.");
		}
		String pid = args[0];
		String agentJarPath = args[1];
		String perftraceConfigFile = args[2];
		VirtualMachine vm = null;
		try {
			vm = VirtualMachine.attach(pid);
			vm.loadAgent(agentJarPath, perftraceConfigFile);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				vm.detach();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
