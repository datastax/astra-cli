package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.exceptions.internal.cli.ExecutionCancelledException;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.NukeOperation;
import com.dtsx.astra.cli.operations.NukeOperation.*;
import com.dtsx.astra.cli.operations.NukeOperation.SkipDeleteReason.NeedsSudo;
import com.dtsx.astra.cli.operations.Operation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.utils.StringUtils.*;

@Command(
    name = "nuke",
    description = "Entirely delete Astra CLI from your system"
)
public class NukeCmd extends AbstractCmd<NukeResult> {
    @Option(
        names = { "--dry-run" },
        description = { "Simulate the nuke operation without deleting any files", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean $dryRun;

    @Option(
        names = { "--preserve-astrarc" },
        description = "Preserve the .astrarc file in your home directory",
        paramLabel = "PRESERVE",
        negatable = true
    )
    public Optional<Boolean> $preserveAstrarc;

    @Option(
        names = { "--yes", "-y" },
        description = { "Whether to nuke without confirmation (if not a dry run)", DEFAULT_VALUE },
        defaultValue = "false",
        fallbackValue = "false"
    )
    public boolean $yes;

    @Override
    protected final OutputAll execute(Supplier<NukeResult> res) {
        ctx.log().banner();
        return handleNuked(res.get());
    }

    private OutputAll handleNuked(NukeResult res) {
        val completeUninstallationMsg = buildCompleteUninstallationMsg(res.binaryDeleteResult());

        if (res.deletedFiles().isEmpty() && res.shellRcFilesToUpdate().isEmpty() && res.skipped().isEmpty()) {
            return OutputAll.response("""
            @|bold Nothing to nuke; no trace of Astra CLI was found.|@
            
            %s
            """.formatted(completeUninstallationMsg));
        }

        val summary = new StringBuilder();
        val nothingToReport = new ArrayList<String>();

        appendToSummary(summary, nothingToReport, "deleted", res.deletedFiles().stream().collect(HashMap::new, (m, v) -> m.put(v, Optional.of(" @|faint (contained binary)|@").filter((_) -> res.cliBinaryPath().startsWith(v))), Map::putAll));
        appendToSummary(summary, nothingToReport, "needing updates", res.shellRcFilesToUpdate().stream().collect(HashMap::new, (m, v) -> m.put(v, Optional.empty()), Map::putAll));
        appendToSummary(summary, nothingToReport, "skipped", res.skipped().entrySet().stream().collect(HashMap::new, (m, v) -> m.put(v.getKey(), Optional.of(" - " + v.getValue().reason())), Map::putAll));

        switch (nothingToReport.size()) {
            case 3 -> summary.append(NL).append("@|faint No files were deleted, updated, or skipped.|@").append(NL);
            case 2 -> summary.append(NL).append("@|faint No files ").append(nothingToReport.get(0)).append(" or ").append(nothingToReport.get(1)).append(".|@").append(NL);
            case 1 -> summary.append(NL).append("@|faint No files ").append(nothingToReport.get(0)).append(".|@").append(NL);
        }

        if (res.skipped().values().stream().anyMatch(s -> s instanceof NeedsSudo)) {
            summary.append(NL).append("""
            @|yellow Warning: Some files were skipped because higher permissions are required to delete them.|@
            @|yellow - You may need to rerun the nuke operation with elevated permissions (for example, using sudo on Unix-based systems).|@
            @|yellow - If any files are managed by another program, e.g. Nix, please update those files using that program’s idiomatic methods.|@
            """).append(NL);
        }

        return OutputAll.response("""
        @|bold Astra CLI Nuke Summary|@%s
        %s
        %s
        """.formatted(
            ($dryRun ? " @|faint (dry run)|@" : ""),
            summary,
            completeUninstallationMsg
        ));
    }

    private String buildCompleteUninstallationMsg(BinaryDeleteResult res) {
        val str = switch (res) {
            case BinaryDeleted() -> """
              @|green Astra CLI binary has been successfully deleted from your system.|@
            """;
            case BinaryMustBeDeleted(var deleteCommand) -> """
              To complete the uninstallation, please run the following command in your terminal:
            
              %s
              %s
        
              @|bold @!Important:!@|@ if you installed @!astra!@ using a package manager (e.g., @!Homebrew!@, @!Nix!@, etc.), @|underline please use the same package manager to uninstall the binary properly|@.
            """.formatted(
              renderComment(ctx.colors(), "If not installed via a package manager"),
              renderCommand(ctx.colors(), deleteCommand)
            );
            case BinaryNotWritable(var path) -> """
              To complete the uninstallation, manually delete @|underline @'!%s!@|@.
              
              @|bold @!Important:!@|@ if you installed @!astra!@ using a package manager (e.g., @!Homebrew!@, @!nix!@, etc.), @|underline please use the same package manager to uninstall the binary properly|@.
              """.formatted(path);
        };

        return trimIndent(str);
    }

    private void appendToSummary(StringBuilder sb, ArrayList<String> nothingToReport, String header, Map<Path, Optional<String>> files) {
        if (files.isEmpty()) {
            nothingToReport.add(header);
        } else {
            sb.append(NL).append("Files ").append(header).append(":").append(NL);

            for (val file : files.entrySet()) {
                sb.append("→ ").append(ctx.highlight(file.getKey()));

                if (file.getValue().isPresent()) {
                    sb.append(file.getValue().get());
                }

                sb.append(NL);
            }
        }
    }

    @Override
    protected Operation<NukeResult> mkOperation() {
        return new NukeOperation(ctx, new NukeRequest(
            $dryRun,
            $preserveAstrarc,
            $yes,
            this::promptShouldDeleteAstrarc,
            this::assertShouldNuke
        ));
    }

    private boolean promptShouldDeleteAstrarc(List<Path> files, boolean isDryRun) {
        val secondLine = (isDryRun)
            ? "@|faint This is a dry-run, so the file @|underline will not actually be deleted|@.|@"
            : "Your credentials @!may be lost!@ if you do. A backup is recommended.";

        val filesStr = new StringJoiner(" and ");

        for (val fileLocation : files) {
            filesStr.add("@|underline @'!" + fileLocation + "!@|@");
        }

        val prompt = """
          Do you also want to delete the @!.astrarc!@ file%s located at %s?
        
          %s
        """.formatted((files.size() > 1) ? "s" : "", filesStr, secondLine);

        return ctx.console().confirm(prompt)
            .defaultNo()
            .fallbackFlag("--preserve-astrarc")
            .fix(originalArgs(), "--[no-]preserve-astrarc")
            .clearAfterSelection();
    }

    private void assertShouldNuke() {
        val prompt = """
          Are you sure you want to entirely delete Astra CLI from your system?
        
          This action is @!irreversible!@ and will delete all Astra CLI files from your system.
        """;

        val shouldNuke = ctx.console().confirm(prompt)
            .defaultNo()
            .fallbackFlag("--yes")
            .fix(originalArgs(), "--yes")
            .clearAfterSelection();

        if (!shouldNuke) {
            throw new ExecutionCancelledException();
        }
    }

    @Override
    protected boolean disableUpgradeNotifier() {
        return true; // who wants to be told there's a new update when they're trying to get rid of the damn thing
    }

    @Override
    protected boolean disableDuplicateFilesCheck() {
        return true; // the deletion algorithm handles this itself
    }
}
