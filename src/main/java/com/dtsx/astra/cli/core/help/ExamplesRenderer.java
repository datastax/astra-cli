package com.dtsx.astra.cli.core.help;

import com.dtsx.astra.cli.core.CliProperties;
import com.dtsx.astra.cli.core.output.AstraColors;
import lombok.experimental.UtilityClass;
import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;

import java.util.ArrayList;
import java.util.List;

import static com.dtsx.astra.cli.utils.StringUtils.*;

@UtilityClass
public class ExamplesRenderer {
    private final String SECTION_HEADINGS_KEY = "examplesHeading";
    private final String SECTION_DETAILS_KEY = "examples";

    // loosely based on https://github.com/remkop/picocli/blob/main/picocli-examples/src/main/java/picocli/examples/customhelp/EnvironmentVariablesSection.java#L54
    public void installRenderer(CommandLine cmd) {
        val examples = cmd.getCommandSpec().userObject().getClass().getAnnotationsByType(Example.class);

        if (examples.length == 0) {
            return;
        }

        cmd.getHelpSectionMap().put(SECTION_HEADINGS_KEY, (help) -> {
            return help.createHeading("%nExamples:%n");
        });

        cmd.getHelpSectionMap().put(SECTION_DETAILS_KEY, (_) -> {
            return renderExamples(examples);
        });

        cmd.setHelpSectionKeys(insertSectionKeys(cmd.getHelpSectionKeys()));
    }

    private static String renderExamples(Example[] examples) {
        val sb = new StringBuilder();

        val colors = new AstraColors(Ansi.AUTO);

        for (val example : examples) {
            for (val comment : example.comment()) {
                sb.append("  ").append(renderComment(colors, comment)).append(NL);
            }

            sb.append("  ").append(renderCommand(colors, example.command().replace("${cli.name}", CliProperties.cliName()))).append(NL);

            for (val output : example.output()) {
                sb.append("  ").append(output).append(NL);
            }

            if (example != examples[examples.length - 1]) {
                sb.append(NL);
            }
        }

        return sb.toString();
    }

    private static List<String> insertSectionKeys(List<String> baseSectionKeys) {
        val index = baseSectionKeys.indexOf(CommandLine.Model.UsageMessageSpec.SECTION_KEY_FOOTER_HEADING);

        return new ArrayList<>(baseSectionKeys) {{
            add(index, SECTION_HEADINGS_KEY);
            add(index + 1, SECTION_DETAILS_KEY);
        }};
    }
}
