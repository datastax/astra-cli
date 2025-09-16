package com.dtsx.astra.cli.testlib.extensions.binaries;

import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MockInstall {
    @MagicConstant(stringValues = { "cqlsh", "dsbulk", "pulsar", "scb" })
    String value();
}
