package com.dtsx.astra.cli.core.docs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AliasForSubcommand {
    Class<?> value();
    class None {} // Special marker class to indicate no aliasing
}
