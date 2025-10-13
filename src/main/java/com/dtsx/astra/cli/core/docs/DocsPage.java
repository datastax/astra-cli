package com.dtsx.astra.cli.core.docs;

import com.dtsx.astra.cli.core.help.Example;
import lombok.val;
import picocli.CommandLine.Model.ArgGroupSpec;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.commands.AbstractCmd.SHOW_CUSTOM_DEFAULT;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

public record DocsPage(List<String> command, DocsPageSections sections, List<DocsPage> subcommands) implements Page {
    public static final String DIRECTORY = "reference";

    @Override
    public String fileName() {
        return "astra-" + String.join("-", command) + ".adoc";
    }

    @Override
    public String filePath() {
        return DIRECTORY + "/" + fileName();
    }

    @Override
    public String render() {
        return
            sections.name.render() + NL + NL +
            sections.synopsis.render() + NL + NL +
            sections.description.render() + NL + NL +
            sections.options.render() + NL + NL +
            sections.examples.render();
    }

    public record DocsPageSections(
        CommandName name,
        CommandSynopsis synopsis,
        CommandDescription description,
        CommandOptions options,
        CommandExamples examples
    ) {}

    interface DocsPageSection {
        String render();
    }

    public record CommandName(List<String> parts) implements DocsPageSection {
        @Override
        public String render() {
            return "= astra " + String.join(" ", parts);
        }
    }

    public record CommandSynopsis(String synopsis) implements DocsPageSection {
        @Override
        public String render() {
            return """
            [source,shell]
            ----
            %s
            ----
            """.formatted(synopsis);
        }
    }

    public record CommandDescription(String[] desc) implements DocsPageSection {
        @Override
        public String render() {
            return Arrays.stream(desc).filter(d -> !d.isBlank()).collect(Collectors.joining(NL + NL));
        }
    }

    public record CommandOptions(List<ArgGroupSpec> hiddenGroups, List<ArgGroupSpec> groupArgs, List<ArgSpec> nonGroupArgs) implements DocsPageSection {
        @Override
        public String render() {
            val sb = new StringBuilder("== Options").append(NL).append(NL);

            val sortedArgs = nonGroupArgs.stream()
                .sorted((a, b) ->
                    (a instanceof PositionalParamSpec)
                        ? ((b instanceof PositionalParamSpec) ? 0 : -1)
                        : ((b instanceof PositionalParamSpec) ? 1 : 0))
                .toList();

            for (val arg : sortedArgs) {
                renderArg(sb, arg);
            }

            for (val group : groupArgs) {
                for (val arg : allNestedArgs(group)) {
                    renderArg(sb, arg);
                }
            }

            for (val group : hiddenGroups) {
                sb.append(NL);
                sb.append('.').append(group.heading().replaceAll("%n|:", "")).append(NL);
                sb.append("[%collapsible").append("]").append(NL);
                sb.append("====").append(NL);
                for (val arg : allNestedArgs(group)) {
                    renderArg(sb, arg);
                }
                sb.append("====").append(NL);
            }

            return sb.toString();
        }

        private void renderArg(StringBuilder sb, ArgSpec arg) {
            val name = (arg instanceof OptionSpec os)
                ? Arrays.stream(os.names()).map((s) -> "`" + s + "`").collect(Collectors.joining(", "))
                : "`" + arg.paramLabel() + "`";

            val desc = Arrays.stream(arg.description())
                .filter(s -> !s.startsWith(SHOW_CUSTOM_DEFAULT))
                .map(s -> NL + "+" + NL + "    " + s)
                .collect(Collectors.joining());

            sb.append("* ").append(name).append(desc).append(NL);
        }

        private List<ArgSpec> allNestedArgs(ArgGroupSpec group) {
            return new ArrayList<>() {{
                addAll(group.allPositionalParametersNested());
                addAll(group.allOptionsNested());
            }};
        }
    }

    public record CommandExamples(Example[] examples) implements DocsPageSection {
        @Override
        public String render() {
            return """
            == Examples
            [source,bash]
            ----
            %s
            ----
            """.formatted(
                Arrays.stream(examples)
                    .map((e) -> "# " + e.comment() + NL + "$ " + e.command() )
                    .collect(Collectors.joining(NL + NL))
            );
        }
    }
}
