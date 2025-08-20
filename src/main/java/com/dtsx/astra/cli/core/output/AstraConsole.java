package com.dtsx.astra.cli.core.output;

import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.core.output.prompters.builders.ConfirmerBuilder;
import com.dtsx.astra.cli.core.output.prompters.builders.PrompterBuilder;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.intellij.lang.annotations.PrintFormat;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Option;

import java.io.Console;
import java.io.PrintStream;
import java.util.regex.Pattern;

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

    public static ConfirmerBuilder confirm(String prompt) {
        return new ConfirmerBuilder(prompt, noInput);
    }

    public static SelectorBuilder select(String prompt) {
        return new SelectorBuilder(prompt, noInput);
    }

    public static PrompterBuilder prompt(String prompt) {
        return new PrompterBuilder(prompt, noInput);
    }

    public static String format(Object... args) {
        val sb = new StringBuilder();

        for (val item : args) {
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
        ps.println(format(items));
    }
}
