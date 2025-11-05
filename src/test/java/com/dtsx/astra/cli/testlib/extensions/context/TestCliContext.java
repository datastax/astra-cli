package com.dtsx.astra.cli.testlib.extensions.context;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.AstraHome;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.datatypes.Ref;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.AstraLogger.Level;
import com.dtsx.astra.cli.core.properties.CliEnvironment;
import com.dtsx.astra.cli.core.properties.CliEnvironmentImpl;
import com.dtsx.astra.cli.core.properties.CliProperties;
import com.dtsx.astra.cli.core.properties.CliPropertiesImpl;
import com.dtsx.astra.cli.testlib.doubles.GatewayProviderMock;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContextOptions.TestCliContextOptionsBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import java.nio.file.Path;
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

        val cliEnv = mkEnvironment();

        val cliProperties = mkProperties(cliEnv, options.homeDir().map((fn) -> fn.apply(options.fs())));

        this.ref = new Ref<>((getCtx) -> new CliContext(
            cliEnv,
            cliProperties,
            options.outputType(),
            new AstraColors(Ansi.OFF),
            new AstraLogger(Level.REGULAR, getCtx, false, Optional.empty(), true),
            mkConsole(inputStream, rawOutput, getCtx),
            new AstraHome(getCtx),
            options.fs(),
            options.gateways(),
            (_) -> { /* no upgrade notifier in tests for obvious reasons */ },
            options.forceProfile()
        ));
    }

    @RequiredArgsConstructor
    private static class FakeCliProperties implements CliProperties {
        @Delegate(excludes = Exclude.class)
        private final CliProperties delegate;

        private final Optional<Path> homeDirPath;

        @Override
        public PathLocations homeFolderLocations(boolean isWindows) {
            if (homeDirPath.isEmpty()) {
                return delegate.homeFolderLocations(isWindows);
            }

            val pathStr = homeDirPath.get().toString();

            return new PathLocations(
                pathStr,
                NEList.of(new PathLocation(pathStr, PathLocationResolver.CUSTOM))
            );
        }

        private interface Exclude {
            PathLocations defaultHomeFolder(boolean isWindows);
        }
    }

    private CliEnvironment mkEnvironment() {
        return new CliEnvironmentImpl() {
            @Override
            public boolean isTty() {
                return true;
            }
        };
    }

    private CliProperties mkProperties(CliEnvironment cliEnv, Optional<Path> homeDir) {
        return new FakeCliProperties(
            CliPropertiesImpl.mkAndLoadSysProps(cliEnv),
            homeDir
        );
    }

    private @NotNull AstraConsole mkConsole(InputStream inputStream, List<OutputLine> outputLines, Supplier<CliContext> getCtx) {
        val readLineImpl = (Supplier<String>) () -> {
            try {
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
