package com.dtsx.astra.cli.core.output;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.CliProperties;
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
import java.io.PrintStream;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class AstraConsole {
    private final Supplier<CliContext> ctxSupplier;
    private final boolean noInput;

    private static final Pattern HIGHLIGHT_PATTERN = Pattern.compile("@!(.*?)!@");

    public InputStream getIn() {
        return System.in;
    }

    public PrintStream getOut() {
        return System.out;
    }

    public PrintStream getErr() {
        return System.err;
    }

    @Getter @Setter
    private @Nullable Console console = System.console();

    @Accessors(fluent = true)
    public static class Mixin {
        @Getter
        @Option(names = "--no-input", description = "Never ask for user input (e.g. confirmation prompts)")
        private boolean noInput;
    }

    public void print(Object... items) {
        if (ctx().outputIsHuman()) {
            throw new CongratsYouFoundABugException("Can not use AstraConsole.print() when the output format is not 'human'");
        }
        write(getOut(), items);
    }

    public void printf(@PrintFormat String format, Object... items) {
        if (ctx().outputIsHuman()) {
            throw new CongratsYouFoundABugException("Can not use AstraConsole.print() when the output format is not 'human'");
        }
        write(getOut(), format.formatted(items));
    }

    public void println(Object... items) {
        if (ctx().outputIsHuman()) {
            throw new CongratsYouFoundABugException("Can not use AstraConsole.println() when the output format is not 'human'");
        }
        writeln(getOut(), items);
    }

    public void error(Object... items) {
        write(getErr(), items);
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

    public String format(Object... args) {
        val sb = new StringBuilder();
        var colorUsed = false;

        for (val item : args) {
            if (item instanceof AstraColor color) {
                sb.append(color.on());
                colorUsed = true;
            } else if (item instanceof String str) {
                val processedStr = HIGHLIGHT_PATTERN.matcher(str.replace("${cli.name}", CliProperties.cliName())).replaceAll((match) ->
                    ctx().highlight(match.group(1)));

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

    private void write(PrintStream ps, Object... items) {
        ps.print(format(items));
        ps.flush();
    }

    private void writeln(PrintStream ps, Object... items) {
        ps.println(format(items));
    }

    private CliContext ctx() {
        return ctxSupplier.get();
    }
}
