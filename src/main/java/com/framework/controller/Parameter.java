package com.framework.controller;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD})
public @interface Parameter {
    String name() default "";

    String description() default "";

    boolean required() default false;

    CommandType type() default CommandType.OBJECT;

    CommandType collectionType() default CommandType.OBJECT;

    Class<?>[] entityType() default Object.class;

    boolean expose() default true;

    boolean includeInApiDoc() default true;

    int length() default 255;

    String since() default "";

    String retrieveMethod() default "getById";

    boolean sendToPool() default true;
}
