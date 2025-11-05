package com.dtsx.astra.cli.core.docs;

import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.docs.DocsPage.CommandAliasingInformation;
import com.dtsx.astra.cli.core.docs.DocsPage.HasAliases;
import com.dtsx.astra.cli.core.docs.DocsPage.IsAliasOf;
import com.dtsx.astra.cli.core.docs.DocsPage.NoAliasingInformation;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import lombok.experimental.UtilityClass;
import lombok.val;
import picocli.CommandLine.Model.CommandSpec;

import java.util.ArrayList;
import java.util.List;

import static com.dtsx.astra.cli.utils.CollectionUtils.listAdd;

@UtilityClass
public class AliasUtils {
    public static CommandAliasingInformation resolveAliasingInformation(List<String> commandFullName, CommandSpec commandSpec) {
        if (!commandSpec.subcommands().isEmpty()) {
            return resolveActualCommandOf(commandFullName, commandSpec);
        }
        return resolveAliasesOf(commandFullName, commandSpec);
    }

    private static CommandAliasingInformation resolveActualCommandOf(List<String> commandFullName, CommandSpec commandSpec) {
        val realClazz = resolveAliasForSubcommandClazz(commandSpec);

        if (realClazz == AliasForSubcommand.None.class) {
            return new NoAliasingInformation();
        }

        val subcommandSpec = commandSpec.subcommands().values().stream()
            .filter(cs -> cs.getCommandSpec().userObject().getClass() == realClazz)
            .findFirst();

        if (subcommandSpec.isEmpty()) {
            throw new CongratsYouFoundABugException("Class '%s' is annotated with @AliasForSubcommand(%s) but %s is not a subcommand".formatted(
                commandSpec.userObject().getClass().getSimpleName(),
                realClazz.getSimpleName(),
                realClazz.getSimpleName()
            ));
        }

        return new IsAliasOf(
            listAdd(commandFullName, subcommandSpec.get().getCommandSpec().name())
        );
    }

    private static CommandAliasingInformation resolveAliasesOf(List<String> commandFullName, CommandSpec commandSpec) {
        val parentFullName = commandFullName.subList(0, commandFullName.size() - 1);
        val aliases = new ArrayList<List<String>>();

        for (val alias : commandSpec.aliases()) {
            aliases.add(listAdd(parentFullName, alias));
        }

        if (commandSpec.parent() != null) {
            if (resolveAliasForSubcommandClazz(commandSpec.parent()) == commandSpec.userObject().getClass()) {
                aliases.add(parentFullName);
            }
        }

        return NEList.parse(aliases)
            .<CommandAliasingInformation>map(HasAliases::new)
            .orElseGet(NoAliasingInformation::new);
    }

    private static Class<?> resolveAliasForSubcommandClazz(CommandSpec commandSpec) {
        val clazz = commandSpec.userObject().getClass();

        if (!Runnable.class.isAssignableFrom(clazz)) {
            return AliasForSubcommand.None.class;
        }

        val aliasAnnotation = clazz.getAnnotation(AliasForSubcommand.class);

        if (aliasAnnotation == null) {
            throw new CongratsYouFoundABugException("Class '%s' has subcommands but is not annotated with @AliasForSubcommand".formatted(clazz.getSimpleName()));
        }

        if (aliasAnnotation.value() == null) {
            throw new CongratsYouFoundABugException("Class '%s' is annotated with @AliasForSubcommand but the value is null".formatted(clazz.getSimpleName()));
        }

        return aliasAnnotation.value();
    }
}
