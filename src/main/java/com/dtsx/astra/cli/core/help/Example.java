package com.dtsx.astra.cli.core.help;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(Examples.class)
public @interface Example {
    String comment() default "";
    String command() default "";
    Class<? extends ExampleProvider> exampleProvider() default NoExampleProvider.class;

    record ResolvedExample(String comment, String command) {}

    interface ExampleProvider {
        static ResolvedExample resolve(Example example, CliContext ctx) {
            if (example.exampleProvider() != NoExampleProvider.class) {
                try {
                    val pair = example.exampleProvider().getDeclaredConstructor().newInstance().get(ctx);
                    return new ResolvedExample(pair.getLeft(), pair.getRight());
                } catch (Exception e) {
                    throw new CongratsYouFoundABugException("Failed to instantiate ExampleProvider: " + example.exampleProvider().getName(), e);
                }
            }

            if (example.comment().isBlank() || example.command().isBlank()) {
                throw new CongratsYouFoundABugException("Example must have either a provider or both comment and command defined.");
            }

            return new ResolvedExample(example.comment(), example.command());
        }

        default Pair<String, String> get(CliContext ctx) {
            throw new CongratsYouFoundABugException("ExampleProvider must implement get method.");
        }
    }

    class NoExampleProvider implements ExampleProvider {}
}
