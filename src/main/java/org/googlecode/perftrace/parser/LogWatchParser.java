package org.googlecode.perftrace.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Hello world!
 * 
 */
public class LogWatchParser {
	
	public static FunctionInfo stringToFuncinfo(String input) {
		String[] funInfoArray = input.split("@");
		FunctionInfo fi = new FunctionInfo();
		fi.funName = funInfoArray[0];
		fi.startTime = Long.parseLong(funInfoArray[1]);
		fi.costTime = Long.parseLong(funInfoArray[2]);
		fi.beginTime = Long.parseLong(funInfoArray[3]);
		fi.endTime = Long.parseLong(funInfoArray[4]);
		fi.remark = funInfoArray[5];
		fi.result = funInfoArray[6];
		fi.funXml = funcinfoToXml(fi, null).funXml;
		// System.out.println("fi="+fi);
		return fi;
	}

	public static FunctionInfo funcinfoToXml(FunctionInfo fi, FunctionInfo fo) {
		FunctionInfo combinFI = new FunctionInfo();
		if (fo == null) {
			String xml = "<" + fi.funName + ">" + "<costTime>" + fi.costTime
					+ "</costTime>" + "<result>" + fi.result + "</result>"
					+ "</" + fi.funName + ">";
			LogWatchComposite lwc = new LogWatchComposite();
			lwc.elapsedTime = fi.costTime;
			lwc.message = fi.remark;
			lwc.startTime = fi.startTime;
			lwc.tag = fi.funName;
			lwc.suffix = fi.result;
			combinFI.funName = fi.funName;
			combinFI.beginTime = fi.beginTime;
			combinFI.endTime = fi.endTime;
			combinFI.costTime = fi.costTime;
			combinFI.startTime = fi.startTime;
			combinFI.remark = fi.remark;
			combinFI.result = fi.result;
			combinFI.funXml = xml;
			combinFI.logWatchComposite = lwc;
			return combinFI;
		} else {
			// todo
			if ((fi.beginTime <= fo.beginTime) && (fi.endTime >= fo.endTime)) {
				combinFI.beginTime = fi.beginTime;
				combinFI.endTime = fi.endTime;
				combinFI.funName = fi.funName;
				combinFI.costTime = fi.costTime;
				int pos = fi.funXml.lastIndexOf("</" + fi.funName + ">");
				String frontStr = fi.funXml.substring(0, pos);
				String endStr = fi.funXml.substring(pos);
				String xml = frontStr + fo.funXml + endStr;
				combinFI.funXml = xml;
				LogWatchComposite lwc = new LogWatchComposite();
				lwc.elapsedTime = fi.costTime;
				lwc.message = fi.remark;
				lwc.startTime = fi.startTime;
				lwc.tag = fi.funName;
				lwc.suffix = fi.result;

				LogWatchComposite lwc1 = new LogWatchComposite();
				lwc1.elapsedTime = fo.costTime;
				lwc1.message = fo.remark;
				lwc1.startTime = fo.startTime;
				lwc1.tag = fo.funName;
				lwc1.suffix = fo.result;
				combinFI.logWatchComposite = fi.logWatchComposite;
				if (combinFI.logWatchComposite == null)
					combinFI.logWatchComposite = lwc;
				if (fo.logWatchComposite == null)
					combinFI.logWatchComposite.addChild(lwc1);
				else
					combinFI.logWatchComposite.addChild(fo.logWatchComposite);
				return combinFI;
			}
			return null;
			// String xml = "";
			// return combinFI;
		}
	}

	public static LogWatchComposite analysis(String input) {
		Stack<FunctionInfo> stack = new Stack<FunctionInfo>();
		String funArray[] = input.split("\\*");

		// 根据结束时间排序
		List<FunctionInfo> funList = new ArrayList<FunctionInfo>();
		for (String t : funArray) {
			FunctionInfo fi = stringToFuncinfo(t);
			funList.add(fi);
		}
		Collections.sort(funList);

		FunctionInfo tempFI = null;
		// for(String fun:funArray){
		for (FunctionInfo fi : funList) {
			// FunctionInfo fi = stringToFuncinfo(fun);

			while (!stack.empty()) {
				FunctionInfo popFI = stack.pop();
				tempFI = funcinfoToXml(fi, popFI);
				if (tempFI == null) {
					// System.out.println("tempFI is null");
					stack.push(popFI);
					break;
				} else {
					fi = tempFI;
				}
			}

			stack.push(fi);
		}
		// System.out.println("stack.size="+stack.size());
		if (stack.size() == 1) {
			// return stack.pop().funXml;
			return stack.pop().logWatchComposite;
		} else {
			return null;// 抛个异常自己写
		}
	}

	public static class FunctionInfo implements Comparable<FunctionInfo> {
		String funName;
		long startTime;
		long beginTime;
		long endTime;
		long costTime;
		String remark;
		String result;// is success
		String funXml;
		LogWatchComposite logWatchComposite;

		@Override
		public String toString() {
			return "FunctionInfo [funName=" + funName + ", startTime="
					+ startTime + ", beginTime=" + beginTime + ", endTime="
					+ endTime + ", costTime=" + costTime + ", remark=" + remark
					+ ", result=" + result + ", funXml=" + funXml + "]";
		}

		public int compareTo(FunctionInfo o) {
			if (o instanceof FunctionInfo) {
				if (endTime > ((FunctionInfo) o).endTime) {
					return 1;
				}
				if (endTime < ((FunctionInfo) o).endTime) {
					return -1;
				}
				return 0;
			} else {
				// 非FunctionInfo对象与之比较,则抛出异常
				throw new ClassCastException("Can't compare");
			}
		}
	}
}
