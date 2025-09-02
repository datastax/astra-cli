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
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.cli.testlib.Fixtures;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;
import com.dtsx.astra.cli.testlib.Fixtures.Roles;
import com.dtsx.astra.cli.testlib.Fixtures.Tokens;
import com.dtsx.astra.cli.testlib.doubles.GatewayProviderMock;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.With;
import lombok.val;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.Help.Ansi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.testlib.AssertUtils.assertIsValidCsvOutput;
import static com.dtsx.astra.cli.testlib.AssertUtils.assertIsValidJsonOutput;
import static com.dtsx.astra.cli.utils.StringUtils.NL;
import static org.mockito.Mockito.*;

public class BaseCmdSnapshotTest {
    private static final Pattern TRAILING_SPACES = Pattern.compile(" +$");

    public interface SnapshotTestOptionsModifier extends Function<SnapshotTestOptionsBuilder, SnapshotTestOptionsBuilder> {}

    @With
    private record SnapshotTestOptions(
        List<String> stdin,
        GatewayProviderMock gateways,
        List<Consumer<GatewayProviderMock>> verifyFns
    ) {}

    public record CmdOutput(
        int exitCode,
        List<OutputLine> rawOutput,
        String[] command,
        SnapshotTestOptions options,
        InputStream inputStream
    ) {
        public String stdout() {
            return rawOutput.stream()
                .filter(StdoutLine.class::isInstance)
                .map(OutputLine::unwrap)
                .collect(Collectors.joining(NL));
        }

        // inspired by https://insta.rs/docs/cmd
        public String toSnapshot() {
            val interleavedOutput = new StringBuilder();

            for (val line : rawOutput) {
                val label = switch (line) {
                    case StdoutLine _ -> "stdout";
                    case StderrLine _ -> "stderr";
                    case StdinLine _ ->  "readln";
                };

                val content = TRAILING_SPACES.matcher(line.unwrap()).replaceAll(m -> "☐".repeat(m.group().length()));

                interleavedOutput.append(NL).append(label).append(": ").append(content);
            }

            return """
            ---- meta ----
            command: astra %s
            exit_code: %d
            ---- output ----%s
            ---- end ----
            """.formatted(
                String.join(" ", command),
                exitCode,
                interleavedOutput
            );
        }
    }

    public sealed interface OutputLine { String unwrap(); }
    public record StdoutLine(String unwrap) implements OutputLine {}
    public record StderrLine(String unwrap) implements OutputLine {}
    public record StdinLine(String unwrap) implements OutputLine {}

    @SuppressWarnings("UnusedReturnValue")
    protected final CmdOutput verifyRun(String cmd, OutputType outputType, SnapshotTestOptionsModifier optionsFn) {
        val output = run(cmd, outputType, optionsFn);

        try {
            switch (outputType) {
                case JSON -> assertIsValidJsonOutput(output.stdout());
                case CSV -> assertIsValidCsvOutput(output.stdout());
            }

            assertInputStreamEmpty(output.inputStream);
            output.options.verifyFns.forEach(fn -> fn.accept(output.options.gateways));

            val approvalsOptions = new Options()
                .forFile().withNamer(new FolderBasedApprovalNamer())
                .forFile().withAdditionalInformation(outputType.name().toLowerCase());

            Approvals.verify(output.toSnapshot(), approvalsOptions);

            return output;
        } catch (Exception e) {
            System.out.println(output.toSnapshot());
            throw e;
        }
    }

    protected final CmdOutput run(String cmd, OutputType outputType, SnapshotTestOptionsModifier optionsMod) {
        val options = optionsMod.apply(mkDefaultOptions()).options;

        val inputStream = mkFakeInput(options.stdin);
        val outputLines = Collections.synchronizedList(new ArrayList<OutputLine>());

        val ctxRef = new Ref<CliContext>((getCtx) -> new CliContext(
            CliEnvironment.unsafeIsWindows(),
            true,
            null,
            new AstraColors(Ansi.OFF),
            new AstraLogger(Level.REGULAR, getCtx, false, Optional.empty()),
            mkConsole(inputStream, getCtx, outputLines),
            new AstraHome(FileSystems.getDefault(), CliEnvironment.unsafeIsWindows()),
            FileSystems.getDefault(),
            options.gateways
        ));

        val cmdParts = buildCmdParts(cmd, outputType);
        val exitCode = AstraCli.run(ctxRef, cmdParts);

        return new CmdOutput(exitCode, outputLines, cmdParts, options, inputStream);
    }

    private @NotNull AstraConsole mkConsole(InputStream inputStream, Supplier<CliContext> getCtx, List<OutputLine> outputLines) {

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

    private String[] buildCmdParts(String cmd, OutputType outputType) {
        val replacements = Map.of(
            "${DatabaseName}", Databases.NameRef.toString(),
            "${RoleName}", Roles.NameRef.toString(),
            "${OrgId}", Fixtures.Organization.getId(),
            "${Token}", Tokens.One.unsafeUnwrap(),
            "${TokenClientId}", Tokens.Created.getClientId()
        );

        for (val e : replacements.entrySet()) {
            cmd = cmd.replace(e.getKey(), e.getValue().replace(" ", "\\ "));
        }

        cmd += (outputType.isNotHuman())
            ? " -o " + outputType.name().toLowerCase()
            : "";

        return Arrays.stream(cmd.split("(?<!\\\\) ")).map(s -> s.replace("\\ ", " ")).filter(s -> !s.isEmpty()).toArray(String[]::new);
    }

    @MustBeInvokedByOverriders
    protected SnapshotTestOptionsBuilder mkDefaultOptions() {
        return new SnapshotTestOptionsBuilder(new SnapshotTestOptions(List.of(), new GatewayProviderMock(), List.of()));
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

    @SneakyThrows
    private void assertInputStreamEmpty(InputStream inputStream) {
        int remaining = inputStream.available();

        if (remaining > 0) {
            byte[] leftover = new byte[remaining];
            int read = inputStream.read(leftover);
            throw new AssertionError("There are " + remaining + " unread bytes in the input stream: '" + new String(leftover, 0, read) + "'");
        }
    }

    @RequiredArgsConstructor
    protected static class SnapshotTestOptionsBuilder {
        private final SnapshotTestOptions options;

        public SnapshotTestOptionsBuilder use(SnapshotTestOptionsModifier modifier) {
            return modifier.apply(this);
        }

        public SnapshotTestOptionsBuilder stdin(List<String> lines) {
            return new SnapshotTestOptionsBuilder(options.withStdin(lines));
        }

        public SnapshotTestOptionsBuilder stdin(String... lines) {
            return stdin(List.of(lines));
        }

        public <G extends SomeGateway> SnapshotTestOptionsBuilder gateway(Class<G> clazz) {
            return gateway(clazz, (_) -> {});
        }

        public <G extends SomeGateway> SnapshotTestOptionsBuilder gateway(Class<G> clazz, Consumer<G> withInstance) {
            val instance = mock(clazz, withSettings().defaultAnswer(RETURNS_SMART_NULLS));
            withInstance.accept(instance);
            return new SnapshotTestOptionsBuilder(options.withGateways(options.gateways.withInstance(instance)));
        }

        public SnapshotTestOptionsBuilder verify(Consumer<GatewayProviderMock> fn) {
            val newVerifyFns = new ArrayList<>(options.verifyFns);
            newVerifyFns.add(fn);
            return new SnapshotTestOptionsBuilder(options.withVerifyFns(newVerifyFns));
        }
    }
}
