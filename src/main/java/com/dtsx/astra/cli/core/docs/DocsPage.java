package com.dtsx.astra.cli.core.docs;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.help.Example.ExampleProvider;
import lombok.val;
import picocli.CommandLine.Model.ArgGroupSpec;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.commands.AbstractCmd.SHOW_CUSTOM_DEFAULT;
import static com.dtsx.astra.cli.utils.CollectionUtils.listConcat;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

public record DocsPage(List<String> command, DocsPageSections sections, List<DocsPage> subcommands) implements Page {
    public static final String DIRECTORY = "commands";

    @Override
    public String fileName() {
        return String.join("-", command) + ".adoc";
    }

    @Override
    public String filePath() {
        return DIRECTORY + "/" + fileName();
    }

    @Override
    public String render() {
        val sj = new StringJoiner(NL + NL);

        val allSections = List.of(
            sections.name,
            sections.synopsis,
            sections.description,
            sections.aliasing,
            sections.subcommands,
            sections.options,
            sections.examples,
            sections.seeAlso
        );

        for (val section : allSections) {
            sj.add(section.render());
        }

        return sj.toString();
    }

    public record DocsPageSections(
        CommandName name,
        CommandSynopsis synopsis,
        CommandDescription description,
        CommandAliasingInformation aliasing,
        CommandSubcommands subcommands,
        CommandOptions options,
        CommandExamples examples,
        CommandSeeAlso seeAlso
    ) {}

    interface DocsPageSection {
        String render();
    }

    public record CommandName(List<String> parts) implements DocsPageSection {
        @Override
        public String render() {
            return "= " + String.join(" ", parts);
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

    public interface CommandAliasingInformation extends DocsPageSection {}

    public record NoAliasingInformation() implements CommandAliasingInformation {
        @Override
        public String render() {
            return "";
        }
    }

    public record HasAliases(NEList<List<String>> aliases) implements CommandAliasingInformation {
        @Override
        public String render() {
            return """
            == Aliases
            
            %s
            """.formatted(
                aliases.stream().map(a -> "`" + String.join(" ", a) + "`").collect(Collectors.joining(", "))
            );
        }
    }

    public record IsAliasOf(List<String> targetCommand) implements CommandAliasingInformation {
        @Override
        public String render() {
            return """
            
            **Alias of `%s`**
            """.formatted(String.join(" ", targetCommand));
        }
    }

    public record CommandSubcommands(List<DocsPage> subcommands) implements DocsPageSection {
        @Override
        public String render() {
            if (subcommands.isEmpty()) {
                return "";
            }

            val sb = new StringBuilder("== Subcommands").append(NL).append(NL);

            for (val subcommand : subcommands) {
                sb.append("* xref:").append(subcommand.fileName()).append("[").append(String.join(" ", subcommand.command())).append("]").append(NL);
            }

            return sb.toString();
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
            if (arg.hidden()) {
                return;
            }

            val name = (arg instanceof OptionSpec os)
                ? Arrays.stream(os.names()).sorted(Comparator.comparing(String::length)).map((s) -> "`" + s + "`").collect(Collectors.joining(", "))
                : "`" + arg.paramLabel() + "`";

            val desc = Arrays.stream(arg.description())
                .filter(s -> !s.startsWith(SHOW_CUSTOM_DEFAULT))
                .collect(Collectors.joining());

            sb.append(name).append(":: ").append(desc).append(NL);
        }

        private List<ArgSpec> allNestedArgs(ArgGroupSpec group) {
            return listConcat(
                group.allPositionalParametersNested(),
                group.allOptionsNested()
            );
        }
    }

    public record CommandExamples(Example[] examples, CliContext ctx) implements DocsPageSection {
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
                    .map(e -> ExampleProvider.resolve(e, ctx))
                    .map((e) -> "# " + e.comment() + NL + "$ " + String.join(" ", e.command()))
                    .collect(Collectors.joining(NL + NL))
            );
        }
    }

    public record CommandSeeAlso(List<String> seeAlsoLinks) implements DocsPageSection {
        @Override
        public String render() {
            if (seeAlsoLinks.isEmpty()) {
                return "";
            }

            val sb = new StringBuilder("== See Also").append(NL).append(NL);
            for (val link : seeAlsoLinks) {
                sb.append("* ").append(link).append(NL);
            }
            return sb.toString();
        }
    }
}
