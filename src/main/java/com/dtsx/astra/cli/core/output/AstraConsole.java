package com.dtsx.astra.cli.core.output;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.output.AstraColors.AstraColor;
import com.dtsx.astra.cli.core.output.prompters.builders.ConfirmerBuilder;
import com.dtsx.astra.cli.core.output.prompters.builders.PrompterBuilder;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.intellij.lang.annotations.PrintFormat;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Option;

import java.io.Console;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class AstraConsole {
    @Getter
    private final InputStream in;

    @Getter
    private final PrintWriter out;

    @Getter
    private final PrintWriter err;

    @Getter
    private final @Nullable Supplier<String> readLineImpl;

    private final Supplier<CliContext> ctxSupplier;
    private final boolean noInput;

    private static final Pattern HIGHLIGHT_PATTERN = Pattern.compile("@!(.*?)!@");
    private static final Pattern HIGHLIGHT_OR_QUOTE_PATTERN = Pattern.compile("@'!(.*?)!@");

    @Getter @Setter
    private @Nullable Console console = System.console();

    @Accessors(fluent = true)
    public static class Mixin {
        @Getter
        @Option(names = "--no-input", description = "Never ask for user input (e.g. confirmation prompts)")
        private boolean noInput;
    }

    public void print(Object... items) {
        if (ctx().outputIsNotHuman()) {
            throw new CongratsYouFoundABugException("Can not use AstraConsole.print() when the output format is not 'human'");
        }
        write(getOut(), items);
    }

    public void printf(@PrintFormat String format, Object... items) {
        if (ctx().outputIsNotHuman()) {
            throw new CongratsYouFoundABugException("Can not use AstraConsole.print() when the output format is not 'human'");
        }
        write(getOut(), format.formatted(items));
    }

    public void println(Object... items) {
        if (ctx().outputIsNotHuman()) {
            throw new CongratsYouFoundABugException("Can not use AstraConsole.println() when the output format is not 'human'");
        }
        writeln(getOut(), items);
    }

    public void unsafePrintln(Object... items) {
        writeln(getOut(), items); // no check on output format
    }

    public void error(Object... items) {
        write(getErr(), items);
    }

    public void errorf(@PrintFormat String format, Object... items) {
        write(getErr(), format.formatted(items));
    }

    public void errorln(Object... items) {
        writeln(getErr(), items);
    }

    public ConfirmerBuilder confirm(String prompt) {
        return new ConfirmerBuilder(ctx(), noInput, prompt);
    }

    public SelectorBuilder select(String prompt) {
        return new SelectorBuilder(ctx(), noInput, prompt);
    }

    public PrompterBuilder prompt(String prompt) {
        return new PrompterBuilder(ctx(), noInput, prompt);
    }

    public String unsafeReadLine(@Nullable String prompt, boolean echoOff) {
        if (console == null) {
            throw new CongratsYouFoundABugException("System.console() is null, unable to read input"); // should only be used internally in prompters
        }

        error(prompt != null ? prompt : "");

        if (readLineImpl != null) {
            return readLineImpl.get();
        }

        return (echoOff)
            ? String.valueOf(console.readPassword())
            : console.readLine();
    }

    public String format(Object... args) {
        val sb = new StringBuilder();
        var colorUsed = false;

        for (val item : args) {
            if (item instanceof AstraColor color) {
                sb.append(color.on());
                colorUsed = true;
            } else if (item instanceof String str) {
                var processedStr = str.replace("${cli.name}", ctx().properties().cliName());

                processedStr = HIGHLIGHT_PATTERN.matcher(processedStr)
                    .replaceAll((match) -> ctx().highlight(match.group(1), false));

                processedStr = HIGHLIGHT_OR_QUOTE_PATTERN.matcher(processedStr)
                    .replaceAll((match) -> ctx().highlight(match.group(1), true));

                sb.append(ctx().colors().ansi().new Text(processedStr, ctx().colorScheme()));
            } else {
                sb.append(item);
            }
        }

        if (colorUsed) {
            sb.append(ctx().colors().reset());
        }

        return sb.toString();
    }

    private void write(PrintWriter ps, Object... items) {
        ps.print(format(items));
        ps.flush();
    }

    private void writeln(PrintWriter ps, Object... items) {
        ps.println(format(items));
    }

    private CliContext ctx() {
        return ctxSupplier.get();
    }
}
