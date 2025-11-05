package com.dtsx.astra.cli.core.output;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.output.prompters.builders.ConfirmerBuilder;
import com.dtsx.astra.cli.core.output.prompters.builders.PrompterBuilder;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.intellij.lang.annotations.PrintFormat;
import org.jetbrains.annotations.Nullable;

import java.io.Console;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.function.Supplier;

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

    @Getter @Setter
    private @Nullable Console console = System.console();

    public void print(String... items) {
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

    public void println(String... items) {
        if (ctx().outputIsNotHuman()) {
            throw new CongratsYouFoundABugException("Can not use AstraConsole.println() when the output format is not 'human'");
        }
        writeln(getOut(), items);
    }

    public void unsafePrintln(String... items) {
        writeln(getOut(), items); // no check on output format
    }

    public void error(String... items) {
        write(getErr(), items);
    }

    public void errorf(@PrintFormat String format, Object... items) {
        write(getErr(), format.formatted(items));
    }

    public void errorln(String... items) {
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

    private void write(PrintWriter ps, String... items) {
        ps.print(ctx().colors().format(items));
        ps.flush();
    }

    private void writeln(PrintWriter ps, String... items) {
        ps.println(ctx().colors().format(items));
    }

    private CliContext ctx() {
        return ctxSupplier.get();
    }
}
