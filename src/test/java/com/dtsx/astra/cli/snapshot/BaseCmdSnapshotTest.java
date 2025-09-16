package com.dtsx.astra.cli.snapshot;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsBuilder;
import com.dtsx.astra.cli.testlib.Fixtures;
import com.dtsx.astra.cli.testlib.Fixtures.*;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext.OutputLine;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext.StderrLine;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext.StdinLine;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext.StdoutLine;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.val;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.snapshot.SnapshotTestOptions.emptySnapshotTestOptionsBuilder;
import static com.dtsx.astra.cli.testlib.AssertUtils.assertIsValidCsvOutput;
import static com.dtsx.astra.cli.testlib.AssertUtils.assertIsValidJsonOutput;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

public class BaseCmdSnapshotTest {
    private static final Pattern TRAILING_SPACES = Pattern.compile(" +$");

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
                    case StdinLine _ -> "readln";
                };

                val content = TRAILING_SPACES.matcher(line.unwrap()).replaceAll(m -> "☐".repeat(m.group().length()));

                interleavedOutput.append(NL).append(label).append(": ").append(content);
            }

            return """
                ---- meta ----
                command: astra %s
                exit_code: %d%s
                ---- output ----%s
                ---- end ----
                """.formatted(
                Arrays.stream(command).map(s -> s.contains(" ") ? "'" + s + "'" : s).collect(Collectors.joining(" ")),
                exitCode,
                options.comments().isEmpty() ? "" : NL + "comments: " + options.comments().stream().map((NL + " | ")::concat).collect(Collectors.joining()),
                interleavedOutput
            );
        }
    }

    @RequiredArgsConstructor
    public static class UnverifiedCmdOutput implements AutoCloseable {
        @Delegate
        private final CmdOutput output;

        private final TestCliContext ctx;

        public void validate() {
            ctx.validate((_, _) -> {});
        }

        @Override
        public void close() {
            ctx.close();
        }
    }

    protected final CmdOutput verifyRun(String cmd, OutputType outputType, Function<SnapshotTestOptionsBuilder, SnapshotTestOptionsBuilder> optionsFn) {
        val output = run(cmd, outputType, optionsFn);

        try {
            output.validate();

            switch (outputType) {
                case JSON -> assertIsValidJsonOutput(output.stdout());
                case CSV -> assertIsValidCsvOutput(output.stdout());
            }

            val approvalsOptions = new Options()
                .forFile().withNamer(new FolderBasedApprovalNamer(getClass())) // override for inherited classes so they don't use parent class name
                .forFile().withAdditionalInformation(outputType.name().toLowerCase());

            Approvals.verify(output.toSnapshot(), approvalsOptions);

            return output.output;
        } catch (Exception e) {
            System.out.println(output.toSnapshot());
            throw e;
        } finally {
            output.close();
        }
    }

    protected final UnverifiedCmdOutput run(String cmd, OutputType outputType, Function<SnapshotTestOptionsBuilder, SnapshotTestOptionsBuilder> optionsMod) {
        val options = optionsMod.apply(emptySnapshotTestOptionsBuilder().outputType(outputType)).build();

        val ctx = new TestCliContext(options);

        val cmdParts = buildCmdParts(cmd, outputType);
        val exitCode = AstraCli.run(ctx.ref(), cmdParts);

        return new UnverifiedCmdOutput(new CmdOutput(exitCode, ctx.rawOutput(), cmdParts, options, ctx.inputStream()), ctx);
    }

    private static final Map<String, String> replacements = new HashMap<>() {{
        put("${DatabaseName}", Databases.NameRef.toString());
        put("${DatabaseId}", Databases.IdRef.toString());
        put("${RoleName}", Roles.NameRef.toString());
        put("${OrgId}", Fixtures.Organization.getId());
        put("${Token}", Fixtures.Token.unsafeUnwrap());
        put("${TokenClientId}", Fixtures.CreateTokenResponse.getClientId());
        put("${EmailRef}", Fixtures.Users.EmailRef.toString());
        put("${TenantName}", Fixtures.Tenants.Name.toString());
        put("${Keyspace}", Databases.Keyspace.name());
        put("${RegionName}", Regions.NAME.unwrap());
        put("${CollectionName}", Collections.Ref.name());
        put("${TableName}", Tables.Ref.name());
    }};

    private String[] buildCmdParts(String cmd, OutputType outputType) {
        for (val e : replacements.entrySet()) {
            cmd = cmd.replace(e.getKey(), e.getValue().replace(" ", "\\ "));
        }

        cmd += (outputType.isNotHuman())
            ? " -o " + outputType.name().toLowerCase()
            : "";

        return Arrays.stream(cmd.split("(?<!\\\\) ")).map(s -> s.replace("\\ ", " ")).filter(s -> !s.isEmpty()).toArray(String[]::new);
    }

    protected String escape(String... args) {
        return Arrays.stream(args).map(s -> s.replace(" ", "\\ ")).collect(Collectors.joining(" "));
    }
}
