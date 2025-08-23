package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.ExecutionCancelledException;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.NukeOperation;
import com.dtsx.astra.cli.operations.NukeOperation.*;
import com.dtsx.astra.cli.operations.Operation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.core.output.ExitCode.EXECUTION_CANCELLED;
import static com.dtsx.astra.cli.utils.StringUtils.NL;
import static com.dtsx.astra.cli.utils.StringUtils.capitalizeFirstLetter;

@Command(
    name = "nuke",
    description = "Entirely delete Astra CLI from your system"
)
public class NukeCmd extends AbstractCmd<NukeResult> {
    @Option(
        names = { "--dry-run" },
        description = "Simulate the nuke operation without deleting any files"
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
        names = { "--cli-name", "-n" },
        description = "The CLI's name (default: ${cli.name})",
        paramLabel = "NAME"
    )
    public Optional<String> $cliName;

    @Option(
        names = { "--yes", "-y" },
        description = "Whether to nuke without confirmation (if not a dry run)",
        defaultValue = "false",
        fallbackValue = "false"
    )
    public boolean $yes;

    @Override
    protected OutputAll execute(Supplier<NukeResult> result) {
        AstraLogger.banner();

        return switch (result.get()) {
            case CouldNotResolveCliName _ -> throwCouldNotResolveCliName();
            case Nuked res -> handleNuked(res);
        };
    }

    private OutputAll handleNuked(Nuked res) {
        val completeUninstallationMsg = (res.finalDeleteCmd().isPresent())
            ? """
              To complete the uninstallation, please run the following command in your terminal:
              
              @|faint # If not installed via a package manager|@
              @!%s!@
              
              @|bold @!Important:!@|@ if you installed @!astra!@ using a package manager (e.g., @!Homebrew!@, @!nix!@, etc.), @|underline please use the same package manager to uninstall the binary properly|@.
              """.formatted(res.finalDeleteCmd().get())
            : """
              To complete the uninstallation, manually delete the Astra CLI binary from your system.
              
              @|bold @!Important:!@|@ if you installed @!astra!@ using a package manager (e.g., @!Homebrew!@, @!nix!@, etc.), @|underline please use the same package manager to uninstall the binary properly|@.
              """;

        if (res.deletedFiles().isEmpty() && res.updatedFiles().isEmpty() && res.skipped().values().stream().allMatch(r -> r instanceof SkipReason.NotFound)) {
            return OutputAll.response("""
            @|bold Nothing to nuke; no trace of Astra CLI was found.|@
            
            If you believe this is an error, please manually:
            - Delete the @!.astra!@ directory in your home folder
            - Delete the @!.astrarc!@ file in your home folder @|faint (if you wish)|@
            
            %s
            """.formatted(completeUninstallationMsg));
        }

        val summary = new StringBuilder();
        val nothingToReport = new ArrayList<String>();

        appendToSummary(summary, nothingToReport, "Deleted files", res.deletedFiles().stream().collect(HashMap::new, (m, v) -> m.put(v, Optional.empty()), Map::putAll));
        appendToSummary(summary, nothingToReport, "Updated files", res.updatedFiles().stream().collect(HashMap::new, (m, v) -> m.put(v, Optional.empty()), Map::putAll));
        appendToSummary(summary, nothingToReport, "Skipped files", res.skipped().entrySet().stream().collect(HashMap::new, (m, v) -> m.put(v.getKey(), Optional.of(v.getValue())), Map::putAll));

        switch (nothingToReport.size()) {
            case 3 -> summary.append("@|faint No files were deleted, updated, or skipped.|@").append(NL);
            case 2 -> summary.append("@|faint No files ").append(nothingToReport.get(0)).append(" or ").append(nothingToReport.get(1)).append(".|@").append(NL);
            case 1 -> summary.append("@|faint No files ").append(nothingToReport.get(0)).append(".|@").append(NL);
        }

        if (res.skipped().values().stream().anyMatch(s -> s instanceof SkipReason.NeedsSudo)) {
            summary.append(NL).append("""
            @|yellow Warning: Some files were skipped because higher permissions are required to delete them.|@
            - You may need to rerun the nuke operation with elevated permissions (for example, using @!sudo!@ on Unix-based systems).
            - If any files are managed by another program, e.g. Nix, please update those files using that program’s idiomatic methods.
            """);
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

    private void appendToSummary(StringBuilder sb, ArrayList<String> nothingToReport, String operation, Map<File, Optional<SkipReason>> files) {
        if (files.isEmpty()) {
            nothingToReport.add(operation);
        } else {
            sb.append(capitalizeFirstLetter(operation)).append(" files").append(":").append(NL);

            for (val file : files.entrySet()) {
                sb.append("→ ").append(highlight(file.getKey().getAbsolutePath()));

                if (file.getValue().isPresent()) {
                    sb.append(" - ").append(file.getValue().get().reason());
                }

                sb.append(NL);
            }

            sb.append(NL);
        }
    }

    private <T> T throwCouldNotResolveCliName() {
        throw new AstraCliException(EXECUTION_CANCELLED, """
          @|bold,red Error: Could not resolve the CLI's name.|@
        
          Please provide it using the @!--cli-name|-n!@ option.
        """, List.of(
            new Hint("Example", "astra-cli nuke -n astra-cli")
        ));
    }

    @Override
    protected Operation<NukeResult> mkOperation() {
        return new NukeOperation(new NukeRequest(
            $dryRun,
            $preserveAstrarc,
            $cliName,
            $yes,
            this::promptShouldDeleteAstrarc,
            this::assertShouldNuke
        ));
    }

    private boolean promptShouldDeleteAstrarc(File file, boolean isDryRun) {
        val secondLine = (isDryRun)
            ? "This is a dry-run, so the file @|underline @!will not actually be deleted!@|@."
            : "Your credentials @!may be lost!@ if you do. A backup is recommended.";

        val prompt = """
          Do you also want to delete the @!.astrarc!@ file located at @|underline @!%s!@|@?
        
          %s
        """.formatted(file.getAbsolutePath(), secondLine);

        return AstraConsole.confirm(prompt)
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

        val shouldNuke = AstraConsole.confirm(prompt)
            .defaultNo()
            .fallbackFlag("--yes")
            .fix(originalArgs(), "--yes")
            .clearAfterSelection();

        if (!shouldNuke) {
            throw new ExecutionCancelledException();
        }
    }
}
