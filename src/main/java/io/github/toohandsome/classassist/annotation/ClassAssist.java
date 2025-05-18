package io.github.toohandsome.classassist.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author hudcan
 */
@Retention(RUNTIME)
@Target({TYPE})
@Documented
public @interface ClassAssist {

    String className() default "";

    boolean useEnv() default false;
}
