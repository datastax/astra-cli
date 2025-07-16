package com.dtsx.astra.cli.core.output;

import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.output.output.OutputType;
import com.dtsx.astra.cli.core.output.select.AstraSelector;
import com.dtsx.astra.cli.core.output.select.SelectStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.graalvm.collections.Pair;
import org.intellij.lang.annotations.PrintFormat;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Option;

import java.io.Console;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;

public class AstraConsole {
    private static final Pattern HIGHLIGHT_PATTERN = Pattern.compile("@!(.*?)!@");

    public static PrintStream getOut() {
        return System.out;
    }

    public static PrintStream getErr() {
        return System.err;
    }

    @Getter @Setter
    private static @Nullable Console console = System.console();

    private static boolean noInput = false;

    public static boolean isTty() {
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
        write(getOut(), items);
    }

    public static void printf(@PrintFormat String format, Object... items) {
        if (OutputType.isNotHuman()) {
            throw new CongratsYouFoundABugException("Can not use AstraConsole.print() when the output format is not 'human'");
        }
        write(getOut(), format.formatted(items));
    }

    public static void println(Object... items) {
        if (OutputType.isNotHuman()) {
            throw new CongratsYouFoundABugException("Can not use AstraConsole.println() when the output format is not 'human'");
        }
        writeln(getOut(), items);
    }

    public static void error(Object... items) {
        write(getErr(), items);
    }

    public static void errorln(Object... items) {
        writeln(getErr(), items);
    }

    public static Optional<String> readLine(String prompt, boolean isSecret) {
        if (console == null || OutputType.isNotHuman() || noInput) {
            return Optional.empty();
        }

        print(trimIndent(prompt), " ");

        val ret = (isSecret)
            ? Optional.ofNullable(console.readPassword()).map(String::valueOf)
            : Optional.ofNullable(console.readLine());

        if (ret.isEmpty()) {
            println();
        }
        println();

        return ret;
    }

    public enum ConfirmResponse {
        ANSWER_OK, ANSWER_NO, NO_ANSWER
    }

    private static final List<String> YES_ANSWERS = List.of("y", "yes", "true", "1", "ok");

    public static ConfirmResponse confirm(String prompt) {
        val read = readLine(prompt, false);

        if (read.isEmpty() || read.get().isBlank()) {
            return ConfirmResponse.NO_ANSWER;
        }

        if (YES_ANSWERS.contains(read.get().trim().toLowerCase())) {
            return ConfirmResponse.ANSWER_OK;
        } else {
            return ConfirmResponse.ANSWER_NO;
        }
    }

    public static Optional<String> select(String prompt, NEList<String> options, String fallback, Pair<? extends Iterable<String>, String> fix, boolean clearAfterSelection) {
        return select(prompt, options, null, Function.identity(), fallback, fix, clearAfterSelection);
    }
    
    public static Optional<String> select(String prompt, NEList<String> options, @Nullable String defaultOption, String fallback, Pair<? extends Iterable<String>, String> fix, boolean clearAfterSelection) {
        return select(prompt, options, defaultOption, Function.identity(), fallback, fix, clearAfterSelection);
    }
    
    public static <T> Optional<T> select(String prompt, NEList<String> options, Function<String, T> mapper, String fallback, Pair<? extends Iterable<String>, String> fix, boolean clearAfterSelection) {
        return select(prompt, options, null, mapper, fallback, fix, clearAfterSelection);
    }

    public static <T> Optional<T> select(String prompt, NEList<String> options, @Nullable String defaultOption, Function<String, T> mapper, String fallback, Pair<? extends Iterable<String>, String> fix, boolean clearAfterSelection) {
        return new AstraSelector().select(trimIndent(prompt), options, Optional.ofNullable(defaultOption), mapper, fallback, fix, clearAfterSelection);
    }

    public static String format(Object... args) {
        val sb = new StringBuilder();

        for (Object item : args) {
            if (item instanceof AstraColors color) {
                sb.append(color.on());
            } else if (item instanceof String str) {
                val processedStr = HIGHLIGHT_PATTERN.matcher(str).replaceAll((match) ->
                    AstraColors.highlight(match.group(1)));

                sb.append(AstraColors.ansi().new Text(processedStr, AstraColors.colorScheme()));
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
        write(ps, items);
        ps.println();
    }
}
