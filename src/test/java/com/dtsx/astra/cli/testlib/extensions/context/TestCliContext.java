package com.dtsx.astra.cli.testlib.extensions.context;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.CliEnvironment;
import com.dtsx.astra.cli.core.config.AstraHome;
import com.dtsx.astra.cli.core.datatypes.Ref;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.AstraLogger.Level;
import com.dtsx.astra.cli.testlib.doubles.GatewayProviderMock;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContextOptions.TestCliContextOptionsBuilder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.Help.Ansi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

@Getter
@Accessors(fluent = true)
public class TestCliContext implements AutoCloseable {
    public sealed interface OutputLine { String unwrap(); }
    public record StdoutLine(String unwrap) implements OutputLine {}
    public record StderrLine(String unwrap) implements OutputLine {}
    public record StdinLine(String unwrap) implements OutputLine {}

    @Delegate
    private final Ref<CliContext> ref;
    private final InputStream inputStream;
    private final List<OutputLine> rawOutput;
    private final TestCliContextOptions options;

    public TestCliContext(TestCliContextOptionsBuilder<?, ?> builder) {
        this(builder.build());
    }

    public TestCliContext(TestCliContextOptions options) {
        this.options = options;

        this.inputStream = mkFakeInput(options.stdin());
        this.rawOutput = Collections.synchronizedList(new ArrayList<>());

        val homeDirPath = options.homeDir()
            .map(fn -> fn.apply(options.fs()))
            .orElseGet(() -> AstraHome.resolveDefaultAstraHomeFolder(options.fs(), CliEnvironment.unsafeResolvePlatform()));

        this.ref = new Ref<>((getCtx) -> new CliContext(
            CliEnvironment.unsafeResolvePlatform(),
            true,
            options.outputType(),
            new AstraColors(Ansi.OFF),
            new AstraLogger(Level.REGULAR, getCtx, false, Optional.empty(), true),
            mkConsole(inputStream, rawOutput, getCtx),
            new AstraHome(homeDirPath),
            options.fs(),
            options.gateways(),
            options.forceProfile()
        ));
    }

    private @NotNull AstraConsole mkConsole(InputStream inputStream, List<OutputLine> outputLines, Supplier<CliContext> getCtx) {
        val readLineImpl = (Function<String, String>) (prompt) -> {
            try {
                Arrays.stream(prompt.split("\\R")).map(StdoutLine::new).forEach(outputLines::add);
                val read = new Scanner(inputStream).nextLine();
                outputLines.add(new StdinLine(read));
                return read;
            } catch (NoSuchElementException e) {
                return null;
            }
        };

        return new AstraConsole(inputStream, mkFakeWriter(outputLines, StdoutLine::new), mkFakeWriter(outputLines, StderrLine::new), readLineImpl, getCtx, false);
    }

    private InputStream mkFakeInput(List<String> lines) {
        return new ByteArrayInputStream((String.join(NL, lines) + (lines.isEmpty() ? "" : NL)).getBytes());
    }

    private PrintWriter mkFakeWriter(List<OutputLine> output, Function<String, OutputLine> constructor) {
        return new PrintWriter(new Writer() {
            @Override
            public void write(char @NotNull [] buf, int off, int len) {
                Arrays.stream(new String(buf, off, len).split("\\R")).map(constructor).forEach(output::add);
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        });
    }

    private boolean validated = false;

    public void validate(BiConsumer<GatewayProviderMock, List<OutputLine>> outputValidator) {
        outputValidator.accept(options.gateways(), rawOutput);

        if (!validated) {
            assertInputStreamEmpty(inputStream);
            options.verifyFns().forEach(fn -> fn.accept(options.gateways()));
        }

        validated = true;
    }

    @Override
    @SneakyThrows
    public void close() {
        val fs = get().fs();

        if (fs.getClass().getName().contains("Jimfs")) {
            fs.close();
        }

        get().console().getIn().close();
        validate((_, _) -> {});
    }

    @SneakyThrows
    private void assertInputStreamEmpty(InputStream inputStream) {
        int remaining = inputStream.available();

        if (remaining > 0) {
            byte[] leftover = new byte[remaining];
            int read = inputStream.read(leftover);
            throw new AssertionError("There are " + remaining + " unread bytes in the input stream: '" + new String(leftover, 0, read) + "'");
        }
    }
}
