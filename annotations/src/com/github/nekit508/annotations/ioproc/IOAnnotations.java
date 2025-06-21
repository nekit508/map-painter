package com.github.nekit508.annotations.ioproc;

import arc.util.io.Reads;
import arc.util.io.Writes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class IOAnnotations {
    /**
     * NOT NECESSARY. This anno used only for adjustment, if class not annotated, all settings will be default.
     * Annotated class must implement {@link IO}.
     * Do not override {@link IO#read(Reads)} and {@link IO#write(Writes)} methods, it's bodies will be fully replaced with generated code.
     * Otherwise, use after/before methods from {@link IO}, to execute your code on reading/writing object.
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface GenObject {
        /** Should super method call will be generated. */
        boolean addSuperCall() default true;

        /**
         * Marks as "io field" annotated field or all fields in annotated class. If field and it's enclosing class annotated with this annotation, settings will be proceeded only field's annotation.
         * Class that contains annotated "io field" must implement {@link IO}.
         */
        @Target({ElementType.FIELD, ElementType.TYPE})
        @Retention(RetentionPolicy.SOURCE)
        @interface Gen {
            /** Should object be auto instantiated before reading. */
            boolean autoCreation() default false;
        }

        /** Totally excepts field from io processing. */
        @Target({ElementType.FIELD})
        @Retention(RetentionPolicy.SOURCE)
        @interface Ignore {
        }
    }

    /** Marks method as "io method" or type as "io method container". */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Provider {
        /** Totally excepts method from io processing. */
        @Target({ElementType.METHOD})
        @Retention(RetentionPolicy.SOURCE)
        @interface Ignore {
        }

        /** Marks method to be generated as uni io provider. */
        @Target({ElementType.METHOD})
        @Retention(RetentionPolicy.SOURCE)
        @interface UniProvider {

        }
    }
}
