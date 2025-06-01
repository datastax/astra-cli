package com.dtsx.astra.cli.output;

import com.dtsx.astra.cli.output.output.OutputType;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.io.Console;
import java.io.PrintStream;
import java.util.Optional;

public class AstraConsole {
    @Getter @Setter
    private static @NonNull PrintStream out = System.out;

    @Getter @Setter
    private static @NonNull PrintStream err = System.err;

    @Getter @Setter
    private static @Nullable Console console = System.console();

    public static void print(Object... items) {
        if (OutputType.isNotHuman()) {
            throw new IllegalStateException("Can not use AstraConsole.print() when the output format is not human");
        }

        for (Object item : items) {
            if (item instanceof AstraColors color) {
                out.print(color.on());
            } else if (item instanceof String str) {
                out.print(AstraColors.ansi().string(str));
            } else {
                out.print(item);
            }
        }
        out.print(AstraColors.reset());
    }

    public static void println(Object... items) {
        print(items);
        out.println();
    }

    public static Optional<String> readLine(String prompt) {
        if (console != null && OutputType.isNotHuman()) {
            val ret = console.readLine(prompt);

            if (ret == null) {
                println();
            }

            return (ret != null)
                ? Optional.of(ret)
                : Optional.empty();
        }

        return Optional.empty();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean confirm(String prompt, boolean default_) {
        return readLine(prompt).orElse(default_ ? "y" : "n").equalsIgnoreCase("y");
    }
}
