package com.dtsx.astra.cli.core.help;

import com.dtsx.astra.cli.commands.CommonOptions.ColorMode;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Ref;
import com.dtsx.astra.cli.core.output.AstraColors;
import lombok.experimental.UtilityClass;
import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;

import java.util.ArrayList;
import java.util.Arrays;
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

        val resolved = Arrays.stream(examples)
            .map(example -> {
                return Example.ExampleProvider.resolve(example, ctxRef.get());
            })
            .toList();

        for (val example : resolved) {
            sb.append("  ").append(renderComment(colors, colors.format(example.comment()))).append(NL);

            sb.append("  ").append(renderCommand(colors, colors.format(joinCommand(example.command())))).append(NL);

            if (example != resolved.getLast()) {
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

    private String joinCommand(String[] command) {
        val sb = new StringBuilder();

        for (val part : command) {
            if (!sb.isEmpty()) {
                sb.append(" \\").append(NL).append("     ");
            }
            sb.append(part);
        }

        return sb.toString();
    }

    // Because --[no-]color is an argument which is not parsed if we are just rendering help,
    // we'll attempt to rudimentarily scan the args to see if color is disabled or not
    private Ansi resolveAnsi(String[] args) {
        for (int i = 0; i < args.length; i++) {
            val arg = args[i];

            if (arg.equals("--color")) {
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    return parseColorMode(args[i + 1]);
                }
                return Ansi.ON;
            }

            if (arg.equals("--no-color")) {
                return Ansi.OFF;
            }

            if (arg.startsWith("--color=")) {
                return parseColorMode(arg.substring("--color=".length()));
            }
        }

        return Ansi.AUTO;
    }

    private Ansi parseColorMode(String mode) {
        try {
            return switch (ColorMode.valueOf(mode.toLowerCase())) {
                case ColorMode.always -> Ansi.ON;
                case ColorMode.never -> Ansi.OFF;
                case ColorMode.auto -> Ansi.AUTO;
            };
        } catch (IllegalArgumentException e) {
            return Ansi.AUTO;
        }
    }
}
