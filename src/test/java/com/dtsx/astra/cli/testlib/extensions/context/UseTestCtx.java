package com.dtsx.astra.cli.testlib.extensions.context;

import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.AstraLogger.Level;
import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface UseTestCtx {
    @MagicConstant(stringValues = { "real", "jimfs", "dummy" })
    String fs() default "dummy";
    AstraLogger.Level logLevel() default Level.REGULAR;
}
