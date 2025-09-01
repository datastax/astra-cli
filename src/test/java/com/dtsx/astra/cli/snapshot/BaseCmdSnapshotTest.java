package com.dtsx.astra.cli.snapshot;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.CliEnvironment;
import com.dtsx.astra.cli.core.config.AstraHome;
import com.dtsx.astra.cli.core.datatypes.Ref;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.AstraLogger.Level;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.cli.testlib.doubles.GatewayProviderMock;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.val;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.Help.Ansi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

public class BaseCmdSnapshotTest {
    public interface SnapshotTestOptionsModifier extends Function<SnapshotTestOptionsBuilder, SnapshotTestOptionsBuilder> {}

    @With
    private record SnapshotTestOptions(
        List<String> stdin,
        GatewayProviderMock gatewayProvider
    ) {}

    public record CmdOutput(
        int exitCode,
        List<OutputLine> rawOutput
    ) {
        public String stdout() {
            return rawOutput.stream()
                .filter(StdoutLine.class::isInstance)
                .map(OutputLine::unwrap)
                .collect(Collectors.joining(NL));
        }

        public String stderr() {
            return rawOutput.stream()
                .filter(StderrLine.class::isInstance)
                .map(OutputLine::unwrap)
                .collect(Collectors.joining(NL));
        }

        public String toSnapshot() {
            return "exit_code: " + exitCode + NL +
                "----- stdout -----" + NL +
                stdout() + NL +
                "----- stderr -----" + NL +
                stderr() + NL;
        }
    }

    public interface OutputLine { String unwrap(); }
    public record StdoutLine(String unwrap) implements OutputLine {}
    public record StderrLine(String unwrap) implements OutputLine {}

    protected final CmdOutput run(String cmd) {
        return run(cmd, b -> b);
    }

    protected final CmdOutput run(String cmd, SnapshotTestOptionsModifier optionsFn) {
        val options = optionsFn.apply(mkDefaultOptions()).options;

        val outputLines = Collections.synchronizedList(new ArrayList<OutputLine>());

        val ctxRef = new Ref<CliContext>((getCtx) -> new CliContext(
            CliEnvironment.unsafeIsWindows(),
            true,
            null,
            new AstraColors(Ansi.OFF),
            new AstraLogger(Level.QUIET, getCtx, false, Optional.empty()),
            new AstraConsole(mkFakeInput(options.stdin), mkFakeWriter(outputLines, StdoutLine::new), mkFakeWriter(outputLines, StderrLine::new), getCtx, false),
            new AstraHome(FileSystems.getDefault(), CliEnvironment.unsafeIsWindows()),
            FileSystems.getDefault(),
            options.gatewayProvider
        ));

        val exitCode = AstraCli.run(ctxRef, cmd.split("(?<!\\\\) "));

        return new CmdOutput(exitCode, outputLines);
    }

    @MustBeInvokedByOverriders
    protected SnapshotTestOptionsBuilder mkDefaultOptions() {
        return new SnapshotTestOptionsBuilder(new SnapshotTestOptions(List.of(), new GatewayProviderMock()));
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

    @RequiredArgsConstructor
    protected static class SnapshotTestOptionsBuilder {
        private final SnapshotTestOptions options;

        public SnapshotTestOptionsBuilder with(SnapshotTestOptionsModifier modifier) {
            return modifier.apply(this);
        }

        public SnapshotTestOptionsBuilder stdin(List<String> lines) {
            return new SnapshotTestOptionsBuilder(options.withStdin(lines));
        }

        public SnapshotTestOptionsBuilder stdin(String... lines) {
            return stdin(List.of(lines));
        }

        public SnapshotTestOptionsBuilder gateway(SomeGateway instance) {
            return new SnapshotTestOptionsBuilder(options.withGatewayProvider(options.gatewayProvider.withInstance(instance)));
        }
    }
}
