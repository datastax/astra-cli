package com.dtsx.astra.cli.unit.core.output;

import com.dtsx.astra.cli.core.datatypes.TriFunction;
import com.dtsx.astra.cli.core.output.Hint;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HintTest {
    @Example
    public void iterable_constructor_works(@ForAll String comment) {
        TriFunction<Iterable<? extends CharSequence>, CharSequence, String, ?> runTest = (cmd, extra, expected) ->
            assertThat(new Hint(comment, cmd, extra)).isEqualTo(new Hint(comment, expected));

        runTest.apply(List.of("kill", "--birds", "2"), "--stones 1", "kill --birds 2 --stones 1");

        runTest.apply(Collections.singleton("subcommand"), "", "subcommand");

        runTest.apply(List.of(), " --flag ", " --flag");

        runTest.apply(List.of(), "--flag", " --flag");

        runTest.apply(List.of("--cars", "3", "--mars", "1"), "", "--cars 3 --mars 1");

        runTest.apply(List.of(), "", "");
    }
}
