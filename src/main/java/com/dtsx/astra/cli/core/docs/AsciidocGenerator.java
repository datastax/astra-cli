package com.dtsx.astra.cli.core.docs;

import com.dtsx.astra.cli.core.docs.DocsPage.*;
import com.dtsx.astra.cli.core.help.Example;
import lombok.RequiredArgsConstructor;
import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.core.docs.AliasUtils.resolveAliasingInformation;
import static com.dtsx.astra.cli.utils.Collectionutils.listAdd;

@RequiredArgsConstructor
public class AsciidocGenerator {
    private final String cliName;
    private final CommandSpec commandSpec;
    private final ExternalDocsSpec docsSpec;

    private int filteredCount = 0; // just for sanity checking to make sure no unused filters or links are present
    private int seeAlsoCount = 0;

    public List<Page> generate() {
        val topLevelDocsPages = generatePagesForSubcommands(
            List.of(cliName),
            subcommandSpecs(commandSpec)
        );

        if (filteredCount != docsSpec.hideCommands().size()) {
            throw new IllegalStateException("Some filters in `spec.hideCommands` did not match any command (expected %d but got %d)".formatted(docsSpec.hideCommands().size(), filteredCount));
        }

        if (seeAlsoCount != docsSpec.seeAlsoLinks().values().stream().mapToInt(List::size).sum()) {
            throw new IllegalStateException("Some links in `spec.seeAlsoLinks` were not used (expected %d but got %d)".formatted(docsSpec.seeAlsoLinks().values().stream().mapToInt(List::size).sum(), seeAlsoCount));
        }

        return listAdd(
            flattenDocsPages(topLevelDocsPages),
            mkNavPage(topLevelDocsPages)
        );
    }

    private List<DocsPage> generatePagesForSubcommands(List<String> parentCommandName, List<CommandSpec> commandSpecs) {
        return commandSpecs.stream()
            .filter((commandSpec) -> {
                val keep = docsSpec.hideCommands().stream().noneMatch(commandSpec.qualifiedName().replaceFirst("astra ", "")::equals);

                if (!keep) {
                    filteredCount++;
                }

                return keep;
            })
            .map((commandSpec) -> {
                val commandFullName = listAdd(parentCommandName, commandSpec.name());

                val subcommands = generatePagesForSubcommands(
                    commandFullName,
                    subcommandSpecs(commandSpec)
                );

                return new DocsPage(
                    commandFullName,
                    mkDocsPageSections(commandFullName, commandSpec, subcommands),
                    subcommands
                );
            })
            .collect(Collectors.toList());
    }

    private NavPage mkNavPage(List<DocsPage> topLevelDocsPages) {
        return new NavPage(topLevelDocsPages);
    }

    private DocsPageSections mkDocsPageSections(List<String> commandFullName, CommandSpec commandSpec, List<DocsPage> subcommands) {
        return new DocsPageSections(
            new CommandName(commandFullName),
            new CommandSynopsis(commandSpec.commandLine().getHelp().synopsis(0)),
            new CommandDescription(commandSpec.usageMessage().description()),
            resolveAliasingInformation(commandFullName, commandSpec),
            new CommandSubcommands(subcommands),
            resolveCommandOptions(commandSpec),
            new CommandExamples(commandSpec.userObject().getClass().getAnnotationsByType(Example.class)),
            resolveSeeAlsoLinks(commandSpec)
        );
    }

    private CommandOptions resolveCommandOptions(CommandSpec commandSpec) {
        val collapsibleGroups = docsSpec.collapsibleOptionGroups();

        return new CommandOptions(
            commandSpec.argGroups().stream().filter((g) -> g.heading() != null && collapsibleGroups.contains(g.heading().replace("%n", ""))).toList(),
            commandSpec.argGroups().stream().filter((g) -> g.heading() == null || !collapsibleGroups.contains(g.heading().replace("%n", ""))).toList(),
            commandSpec.args().stream().filter((a) -> a.group() == null).toList()
        );
    }

    private CommandSeeAlso resolveSeeAlsoLinks(CommandSpec commandSpec) {
        val seeAlsoLinksSpec = docsSpec.seeAlsoLinks();
        val commandKey = commandSpec.qualifiedName().replaceFirst("astra ", "");

        if (seeAlsoLinksSpec.containsKey(commandKey)) {
            seeAlsoCount += seeAlsoLinksSpec.get(commandKey).size();
            return new CommandSeeAlso(seeAlsoLinksSpec.get(commandKey));
        } else {
            return new CommandSeeAlso(List.of());
        }
    }

    private List<CommandSpec> subcommandSpecs(CommandSpec commandSpec) {
        return commandSpec.subcommands().values().stream()
            .map(CommandLine::getCommandSpec)
            .distinct()
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
