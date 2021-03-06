package org.dddjava.jig.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
@Repeatable(JigNotes.class)
public @interface JigNote {

    String name();

    String value() default "";
}
