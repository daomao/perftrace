/**
 * 
 */
package org.googlecode.perftrace.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.googlecode.perftrace.PerfTrace;
import org.googlecode.perftrace.filter.commons.codec.binary.Base64;
import org.googlecode.perftrace.perf4j.LoggingStopWatch;

/**
 * 
 * modified by zhongfeng,添加性能监控
 */
public abstract class PerftraceFilter implements Filter {
	
	public static final String ROOT_TOTAL_WATCH = "RootTotalWatch";

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig arg0) throws ServletException {
	}


	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		getPerftrace().addAdditionMsg(request.getRemoteAddr());
		getPerftrace().addAdditionMsg(getChannel((HttpServletRequest)request));
		getPerftrace().addAdditionMsg(getHttpBasicAuthUserName((HttpServletRequest) request));
		
		//调用计时日志服务
		LoggingStopWatch watch = createLoggingStopWatch();
		try{
			chain.doFilter(request, response);
		}finally{
			watch.stop();
		}
	}
	
	/**
	 * @return LoggingStopWatch的子类有JavaLog Slf4j Log4j
	 */
	protected abstract LoggingStopWatch createLoggingStopWatch();

	/**
	 * @return 返回Perftrace子类，支持的类型有JavaLog Slf4j Log4j
	 */
	protected abstract PerfTrace getPerftrace();

	/**
	 * OTA验证及权限部分是基于context之后的部分做的
	 * 此方法用于截取URI的context以后部分
	 * 
	 * @param request
	 * @return
	 */
	private String getChannel(HttpServletRequest request){
		
		String realUri = ((HttpServletRequest)request).getRequestURI();
		
		String ctxStr = ((HttpServletRequest)request).getContextPath();
		
		if(realUri.length() > ctxStr.length())
			return realUri.substring(ctxStr.length());
		else
			return "/"; // 两者相等时返回"/"，避免返回""，AuthManagerImpl验证时要求至少是从"/"开头
	}
	
	private static String getHttpBasicAuthUserName(
			HttpServletRequest request) {

		String basicAuthString = (String) request.getHeader("Authorization");

		String decodedBasicAuthString = null;
		String username = "NON";
		String password = null;
		if (basicAuthString != null) {
			basicAuthString = basicAuthString.trim();

			if (basicAuthString.startsWith("Basic ")) {
				decodedBasicAuthString = new String(Base64
						.decodeBase64(basicAuthString.substring(6).getBytes()));
			}

			int collonIndex = decodedBasicAuthString.indexOf(':');

			if (collonIndex == -1) {
				username = decodedBasicAuthString;
			} else {
				username = decodedBasicAuthString.substring(0, collonIndex);
				password = decodedBasicAuthString.substring(collonIndex + 1);
			}
		}
		return username;
	}


	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
	}
	
}
