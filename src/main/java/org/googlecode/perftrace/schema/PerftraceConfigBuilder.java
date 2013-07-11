package org.googlecode.perftrace.schema;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * @author zhongfeng
 * 
 */
public class PerftraceConfigBuilder {
	private final static Logger logger = Logger
			.getLogger(PerftraceConfigBuilder.class.getName());

	public static PerftraceConfig getPerftraceConfig(String fileName) {
		PerftraceConfig config = new PerftraceConfig();
		try {
			JAXBContext context = JAXBContext
					.newInstance(PerftraceConfig.class);
			File f = new File(fileName);
			if (f != null) {
				Unmarshaller unMarshaller = context.createUnmarshaller();
				config = (PerftraceConfig) unMarshaller.unmarshal(f);
				logger.log(Level.INFO, "Load perftrace.xml from {}"
						+ f.getAbsolutePath());
			}
		} catch (JAXBException e) {
			logger.log(Level.SEVERE, "Unmarshaller perftrace.xml error", e);
		}
		return config;
	}

}
