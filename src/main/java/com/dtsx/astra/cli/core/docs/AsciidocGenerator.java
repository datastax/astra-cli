package com.dtsx.astra.cli.core.docs;

import com.dtsx.astra.cli.core.docs.DocsPage.*;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import com.dtsx.astra.cli.core.help.Example;
import lombok.RequiredArgsConstructor;
import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class AsciidocGenerator {
    private final CommandSpec commandSpec;
    private final LinkedHashMap<String, String> navMappings;

    public List<Page> generate() {
        val topLevelDocsPages = generatePagesForSubcommands(
            List.of(),
            subcommandSpecs(commandSpec).stream()
                .filter(spec -> !spec.subcommands().isEmpty())
                .toList()
        );

        return new ArrayList<>() {{
            addAll(flattenDocsPages(topLevelDocsPages));
            add(generateNavPage(navMappings, topLevelDocsPages));
        }};
    }

    private List<DocsPage> generatePagesForSubcommands(List<String> parentCommandName, List<CommandSpec> commandSpecs) {
        return commandSpecs.stream().map(commandSpec -> {
            val commandFullName = new ArrayList<>(parentCommandName) {{
                add(commandSpec.name());
            }};

            val subcommands = generatePagesForSubcommands(
                commandFullName,
                subcommandSpecs(commandSpec)/*.stream()
                    .filter(spec -> spec.subcommands().isEmpty())
                    .toList()*/
            );

            return new DocsPage(
                commandFullName,
                generateAsciidocForCommand(commandFullName, commandSpec),
                subcommands
            );
        }).collect(Collectors.toList());
    }

    private NavPage generateNavPage(LinkedHashMap<String, String> navMappings, List<DocsPage> topLevelDocsPages) {
        if (navMappings.size() != topLevelDocsPages.size()) {
            throw new OptionValidationException("nav mappings", "Nav mappings size (" + navMappings.size() + ") does not match top level commands size (" + topLevelDocsPages.size() + ")");
        }

        return new NavPage(navMappings.sequencedEntrySet().stream().collect(
            LinkedHashMap::new,
            (map, entry) -> map.put(entry.getKey(), topLevelDocsPages.stream()
                .filter(page -> page.command().getFirst().equals(entry.getValue()))
                .findFirst()
                .orElseThrow(() -> new OptionValidationException("nav mappings", "No page found for subcommand '" + entry.getValue() + "'"))
            ),
            Map::putAll
        ));
    }

    private DocsPageSections generateAsciidocForCommand(List<String> commandFullName, CommandSpec commandSpec) {
        return new DocsPageSections(
            new CommandName(commandFullName),
            new CommandSynopsis(commandSpec.commandLine().getHelp().synopsis(0)),
            new CommandDescription(commandSpec.usageMessage().description()),
            new CommandOptions(
                commandSpec.argGroups().stream().filter((g) -> g.heading() != null && (g.heading().contains("Common Options") || g.heading().contains("Connection Options"))).toList(),
                commandSpec.argGroups().stream().filter((g) -> g.heading() == null || !(g.heading().contains("Common Options") || g.heading().contains("Connection Options"))).toList(),
                commandSpec.args().stream().filter((a) -> a.group() == null).toList()
            ),
            new CommandExamples(commandSpec.userObject().getClass().getAnnotationsByType(Example.class))
        );
    }

    private List<CommandSpec> subcommandSpecs(CommandSpec commandSpec) {
        return commandSpec.subcommands().values().stream()
            .map(CommandLine::getCommandSpec)
            .toList();
    }

    private List<DocsPage> flattenDocsPages(List<DocsPage> pages) {
        return pages.stream()
            .flatMap((page) -> {
                return Stream.concat(
                    Stream.of(page),
                    flattenDocsPages(page.subcommands()).stream()
                );
            })
            .toList();
    }
}
