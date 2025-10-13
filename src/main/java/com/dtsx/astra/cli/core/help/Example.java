package com.dtsx.astra.cli.core.help;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(Examples.class)
public @interface Example {
    String comment();
    String command();
}
