package com.travelsky.perftrace.test.util.sresource.resource;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamSource {

	InputStream getInputStream() throws IOException;

}
