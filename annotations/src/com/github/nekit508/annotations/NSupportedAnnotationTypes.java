package com.github.nekit508.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NSupportedAnnotationTypes {
    Class<? extends Annotation>[] value();
}