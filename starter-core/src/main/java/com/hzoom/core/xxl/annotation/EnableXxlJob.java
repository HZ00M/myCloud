package com.hzoom.core.xxl.annotation;

import com.hzoom.core.xxl.autoconfig.XxlAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(XxlAutoConfiguration.class)
public @interface EnableXxlJob {
}
