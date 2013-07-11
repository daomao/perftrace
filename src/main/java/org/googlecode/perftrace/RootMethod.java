package org.googlecode.perftrace;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 标记接口，用于标记一个方法是调用栈的起始点
 * 
 * @author zhongfeng
 * 
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface RootMethod {
}
