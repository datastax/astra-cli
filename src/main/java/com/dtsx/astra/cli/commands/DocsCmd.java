package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.docs.AsciidocGenerator;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.operations.Operation;
import lombok.val;
import org.apache.commons.io.file.PathUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.AstraColors.stripAnsi;
import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;

@Command(
    name = "docs",
    aliases = { "docgen" },
    hidden = true
)
public class DocsCmd extends AbstractCmd<Void> {
    @Option(
        names = { "--output-dir" },
        description = "Output directory for generated documentation",
        defaultValue = "build/docs"
    )
    public Path outputDir;

    @Override
    protected OutputHuman executeHuman(Supplier<Void> v) {
        val generated = ctx.log().loading("Generating documentation tree", (_) -> {
            return new AsciidocGenerator(spec.root(), sequencedMapOf(
                "CLI configuration", "config",
                "Manage your Databases", "db",
                "Manage Astra Streaming", "streaming",
                "Work with your organization", "org",
                "Work with your tokens", "token",
                "Work with users", "user",
                "Work with roles", "role"
            )).generate();
        });

        ctx.log().loading("Writing documentation to " + outputDir, (_) -> {
            try {
                Files.createDirectories(outputDir);
                PathUtils.cleanDirectory(outputDir);

                for (val doc : generated) {
                    val filePath = outputDir.resolve(doc.filePath());
                    Files.createDirectories(filePath.getParent());
                    Files.writeString(filePath, stripAnsi(ctx.console().format(doc.render())));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        });

        return OutputAll.response("Wrote @!" + generated.size() + "!@ files to @'!" + outputDir + "!@");
    }

    @Override
    protected Operation<Void> mkOperation() {
        return null;
    }

    @Override
    protected boolean disableUpgradeNotifier() {
        return true;
    }
}
