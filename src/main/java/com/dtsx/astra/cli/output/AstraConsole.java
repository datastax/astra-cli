package com.dtsx.astra.cli.output;

import com.dtsx.astra.cli.exceptions.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.output.output.OutputType;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.*;

import java.io.Console;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

public class AstraConsole {
    @Getter @Setter
    private static PrintStream out = System.out;

    @Getter @Setter
    private static PrintStream err = System.err;

    @Getter @Setter
    private static @Nullable Console console = System.console();

    private static boolean noInput = false;

    public static boolean isTty() {
        // Check current console state, not just the cached one
        return System.console() != null;
    }

    public static class Mixin {
        @Option(names = "--no-input", description = "Never ask for user input (e.g. confirmation prompts)")
        public void setNoInput(boolean noInput) {
            AstraConsole.noInput = noInput;
        }
    }

    public static void print(Object... items) {
        if (OutputType.isNotHuman()) {
            throw new CongratsYouFoundABugException("Can not use AstraConsole.print() when the output format is not 'human'");
        }
        write(out, items);
    }

    public static void println(Object... items) {
        if (OutputType.isNotHuman()) {
            throw new CongratsYouFoundABugException("Can not use AstraConsole.println() when the output format is not 'human'");
        }
        writeln(out, items);
    }

    public static void error(Object... items) {
        write(err, items);
    }

    public static void errorln(Object... items) {
        writeln(err, items);
    }

    public static Optional<String> readLine(String prompt) {
        if (console == null || OutputType.isNotHuman() || noInput) {
            return Optional.empty();
        }

        val ret = console.readLine(prompt);

        if (ret == null) {
            println();
        }

        return Optional.ofNullable(ret);
    }

    public enum ConfirmResponse {
        ANSWER_OK, ANSWER_NO, NO_ANSWER
    }

    private static final List<String> YES_ANSWERS = List.of("y", "yes", "true", "1", "ok");

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static ConfirmResponse confirm(String prompt) {
        val read = readLine(format(prompt));

        if (read.isEmpty()) {
            return ConfirmResponse.NO_ANSWER;
        }

        println("--");

        if (YES_ANSWERS.contains(read.get().trim().toLowerCase())) {
            return ConfirmResponse.ANSWER_OK;
        } else {
            return ConfirmResponse.ANSWER_NO;
        }
    }

    public static String format(Object... args) {
        val sb = new StringBuilder();

        for (Object item : args) {
            if (item instanceof AstraColors color) {
                sb.append(color.on());
            } else if (item instanceof String str) {
                sb.append(AstraColors.ansi().string(str));
            } else {
                sb.append(item);
            }
        }

        sb.append(AstraColors.reset());
        return sb.toString();
    }

    private static void write(PrintStream ps, Object... items) {
        ps.print(format(items));
        ps.flush();
    }

    private static void writeln(PrintStream ps, Object... items) {
        print(items);
        ps.println();
    }
}
