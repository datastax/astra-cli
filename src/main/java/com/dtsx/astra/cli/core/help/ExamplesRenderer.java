package com.dtsx.astra.cli.core.help;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Ref;
import com.dtsx.astra.cli.core.output.AstraColors;
import lombok.experimental.UtilityClass;
import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.dtsx.astra.cli.utils.StringUtils.*;

@UtilityClass
public class ExamplesRenderer {
    private final String SECTION_HEADINGS_KEY = "examplesHeading";
    private final String SECTION_DETAILS_KEY = "examples";

    // loosely based on https://github.com/remkop/picocli/blob/main/picocli-examples/src/main/java/picocli/examples/customhelp/EnvironmentVariablesSection.java#L54
    public void installRenderer(CommandLine cmd, String[] args, Ref<CliContext> ctxRef) {
        val examples = cmd.getCommandSpec().userObject().getClass().getAnnotationsByType(Example.class);

        if (examples.length == 0) {
            return;
        }

        cmd.getHelpSectionMap().put(SECTION_HEADINGS_KEY, (help) -> {
            return help.createHeading("%nExamples:%n");
        });

        cmd.getHelpSectionMap().put(SECTION_DETAILS_KEY, (_) -> {
            return renderExamples(examples, args, ctxRef);
        });

        cmd.setHelpSectionKeys(insertSectionKeys(cmd.getHelpSectionKeys()));
    }

    private String renderExamples(Example[] examples, String[] args, Ref<CliContext> ctxRef) {
        val sb = new StringBuilder();

        val colors = new AstraColors(resolveAnsi(args));

        for (val example : examples) {
            sb.append("  ").append(renderComment(colors, example.comment())).append(NL);

            sb.append("  ").append(renderCommand(colors, example.command()
                .replace("${cli.name}", ctxRef.get().properties().cliName())
                .replace("${cli.path}", ctxRef.get().properties().binaryPath().map(Path::toString).orElse("/path/to/cli"))
            )).append(NL);

            if (example != examples[examples.length - 1]) {
                sb.append(NL);
            }
        }

        return sb.toString();
    }

    private List<String> insertSectionKeys(List<String> baseSectionKeys) {
        val index = baseSectionKeys.indexOf(CommandLine.Model.UsageMessageSpec.SECTION_KEY_FOOTER_HEADING);

        return new ArrayList<>(baseSectionKeys) {{
            add(index, SECTION_HEADINGS_KEY);
            add(index + 1, SECTION_DETAILS_KEY);
        }};
    }

    // Because --[no-]color is an argument which is not parsed if we are just rendering help,
    // we'll attempt to rudimentarily scan the args to see if color is disabled or not
    private Ansi resolveAnsi(String[] args) {
        for (val arg : args) {
            if (arg.equals("--color")) {
                return Ansi.ON;
            }

            if (arg.equals("--no-color")) {
                return Ansi.OFF;
            }

            if (arg.startsWith("--color=")) {
                val v = arg.substring("--color=".length());

                return (v.equalsIgnoreCase("false"))
                    ? Ansi.OFF
                    : Ansi.ON;
            }
        }

        return Ansi.AUTO;
    }
}
