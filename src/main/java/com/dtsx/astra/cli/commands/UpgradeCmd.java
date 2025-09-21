package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.exceptions.internal.cli.ExecutionCancelledException;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.UpgradeOperation;
import com.dtsx.astra.cli.operations.UpgradeOperation.UpgradeRequest;
import lombok.SneakyThrows;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.utils.StringUtils.renderCommand;
import static com.dtsx.astra.cli.utils.StringUtils.renderComment;

@Command(
    name = "upgrade",
    description = "Upgrade your Astra CLI installation"
)
public class UpgradeCmd extends AbstractCmd<Void> {
    @Option(
        names = { "-t", "--tag" },
        description = "Version to upgrade to (default: latest)"
    )
    public Optional<String> $version;

    @Option(
        names = { "-y", "--yes" },
        description = "Install the upgrade without any confirmation prompting"
    )
    public boolean $yes;

    @Override
    @SneakyThrows
    protected OutputHuman executeHuman(Supplier<Void> v) {
        v.get();

        return OutputHuman.response("");
    }

    protected void confirmUpgrade(String version, String moveCommand) {
        val infoMsg = """
          Astra CLI @!%s!@ has been downloaded, and is ready to be installed.
        
          %s
          %s
          %s
        
          %s
          %s
        """.formatted(
            version,
            renderComment(ctx.colors(), "The current executable will be replaced by the new one,"),
            renderComment(ctx.colors(), "But in the case the move fails, you can manually run the following:"),
            renderCommand(ctx.colors(), moveCommand),
            renderComment(ctx.colors(), "Check if the installation was successful by running:"),
            renderCommand(ctx.colors(), "astra --version")
        );

        ctx.console().println(infoMsg);

        if (!$yes) {
            val proceed = ctx.console().confirm("%n%nDo you want to proceed?")
                .defaultYes()
                .fallbackFlag("--yes")
                .fix(originalArgs(), "--yes")
                .clearAfterSelection();

            if (!proceed) {
                throw new ExecutionCancelledException();
            }
        }
    }

    @Override
    protected Operation<Void> mkOperation() {
        val downloadsGateway = ctx.gateways().mkDownloadsGateway(ctx);
        return new UpgradeOperation(ctx, downloadsGateway, new UpgradeRequest($version, this::confirmUpgrade));
    }
}
