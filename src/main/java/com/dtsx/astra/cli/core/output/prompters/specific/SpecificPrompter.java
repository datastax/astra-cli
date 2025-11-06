package com.dtsx.astra.cli.core.output.prompters.specific;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsClearAfterSelection;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsFallback;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.val;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.core.output.AstraColors.stripAnsi;

public class SpecificPrompter {
    @Data
    @Builder
    @Accessors(fluent = true)
    public static class Options<T, R> {
        @NonNull String thing;
        @NonNull String prompt;
        @NonNull ExitCode thingNotFoundCode;
        @NonNull Supplier<List<T>> thingsSupplier;
        @NonNull Function<T, String> getThingIdentifier;
        BiFunction<T, Boolean, String> getThingDisplayExtra;
        Function<NEList<T>, NEList<T>> modifier;
        @NonNull Function<NeedsFallback<T>, NeedsClearAfterSelection<T>> fix;
        @NonNull Function<T, R> mapSingleFound;
        Function<T, R> mapMultipleFound;
    }

    public static <T, R> R run(CliContext ctx, Function<Options.OptionsBuilder<T, R>, Options.OptionsBuilder<T, R>> builderFn) {
        val options = builderFn.apply(Options.builder()).build();

        if (options.modifier == null) {
            options.modifier = (things) -> things;
        }

        if (options.getThingDisplayExtra == null) {
            options.getThingDisplayExtra = (_, _) -> "";
        }

        if (options.mapMultipleFound == null) {
            options.mapMultipleFound = (_) -> { throw new CongratsYouFoundABugException("Selector for " + options.thing + " did not expect multiple " + options.thing + "s to have the same thing"); };
        }

        val things = options.modifier.apply(
            NEList.parse(options.thingsSupplier.get()).orElseThrow(() -> new AstraCliException(options.thingNotFoundCode, "@|bold,red Error: no " + options.thing + "s found to select from|@"))
        );

        val namesAreUnique = things.stream()
            .map(options.getThingIdentifier)
            .distinct()
            .count() == things.size();

        val maxNameLength = things.stream()
            .map(t -> options.getThingIdentifier.apply(t).length())
            .max(Integer::compareTo)
            .orElse(0);

        val thingToDisplayMap = things.stream().collect(Collectors.toMap(
            t -> t,
            t -> {
                val extra = options.getThingDisplayExtra.apply(t, namesAreUnique);

                return options.getThingIdentifier.apply(t) + " ".repeat(maxNameLength - options.getThingIdentifier.apply(t).length()) +
                    (extra.isEmpty()
                        ? "" :
                    (stripAnsi(extra).equals(extra))
                        ? " " + ctx.colors().NEUTRAL_500.use("(" + extra + ")")
                        : " " + extra);
            }
        ));

        val selected = options.fix.apply(
            ctx.console().select(options.prompt)
                .options(things)
                .requireAnswer()
                .mapper(thingToDisplayMap::get)
        ).clearAfterSelection();

        val multipleThingsMatch = things.stream().filter(t -> options.getThingIdentifier.apply(t).equals(options.getThingIdentifier.apply(selected))).count() > 1;

        return (multipleThingsMatch)
            ? options.mapMultipleFound.apply(selected)
            : options.mapSingleFound.apply(selected);
    }
}
