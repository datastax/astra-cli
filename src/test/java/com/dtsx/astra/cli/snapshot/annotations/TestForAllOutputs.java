package com.dtsx.astra.cli.snapshot.annotations;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ParameterizedTest
@EnumSource(OutputType.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestForAllOutputs {}
