package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.docs.AsciidocGenerator;
import com.dtsx.astra.cli.core.docs.ExternalDocsSpec;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.utils.JsonUtils;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.file.PathUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi.IStyle;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.AstraColors.stripAnsi;

@Command(
    name = "docs",
    aliases = { "docgen" },
    description = "Generates asciidoc command reference. Meant for internal use.",
    hidden = true
)
public class DocsCmd extends AbstractCmd<Void> {
    @Option(
        names = { "--output-dir" },
        description = "Output directory for generated documentation",
        defaultValue = "build/docs"
    )
    public Path outputDir;

    @Option(
        names = { "--spec", "-s" },
        description = "Path to the documentation specification file",
        defaultValue = "docs_spec.json"
    )
    public Path specFile;

    @Override
    @SneakyThrows
    protected OutputHuman executeHuman(Supplier<Void> v) {
        if (!Files.exists(specFile)) {
            throw new OptionValidationException("spec file", "Specified path does not exist: " + specFile);
        }

        val docsSpec = JsonUtils.objectMapper().readValue(
            specFile.toFile(),
            ExternalDocsSpec.class
        );

        val generated = ctx.log().loading("Generating documentation tree", (_) -> {
            return new AsciidocGenerator(ctx, spec.root(), docsSpec).generate();
        });

        ctx.log().loading("Writing documentation to " + outputDir, (_) -> {
            try {
                Files.createDirectories(outputDir);
                PathUtils.cleanDirectory(outputDir);

                val colors = mkModifiedColors();

                for (val doc : generated) {
                    val filePath = outputDir.resolve(doc.filePath());
                    Files.createDirectories(filePath.getParent());
                    Files.writeString(filePath, stripAnsi(colors.format(doc.render())));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        });

        return OutputAll.response("Wrote @!" + generated.size() + "!@ files to @'!" + outputDir + "!@");
    }

    private AstraColors mkModifiedColors() {
        return new AstraColors(ctx.colors().ansi(), (b) -> {
            val markupMap = new HashMap<>(b.customMarkupMap());

            markupMap.put("code", new IStyle() {
                public String on() { return "``"; }
                public String off() { return "``"; }
            });

            markupMap.put("bold", new IStyle() {
                public String on() { return "**"; }
                public String off() { return "**"; }
            });

            markupMap.put("italic", new IStyle() {
                public String on() { return "__"; }
                public String off() { return "__"; }
            });

            markupMap.put("underline", new IStyle() {
                public String on() { return "[.underline]#"; }
                public String off() { return "#"; }
            });

            return b.customMarkupMap(markupMap);
        });
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
