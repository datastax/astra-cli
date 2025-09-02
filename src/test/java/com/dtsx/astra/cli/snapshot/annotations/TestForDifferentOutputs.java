package com.dtsx.astra.cli.snapshot.annotations;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ParameterizedTest
@EnumSource(value = OutputType.class, names = { "HUMAN", "JSON" })
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestForDifferentOutputs {} // CSV output is pretty similar to JSON... as long as we test CSV here and there, we can skip it in most tests to save time
