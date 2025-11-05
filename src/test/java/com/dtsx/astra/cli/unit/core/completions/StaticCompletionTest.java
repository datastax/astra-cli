package com.dtsx.astra.cli.unit.core.completions;

import com.dtsx.astra.cli.core.completions.StaticCompletion;
import lombok.val;
import net.jqwik.api.ForAll;
import net.jqwik.api.Group;
import net.jqwik.api.Property;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Group
public class StaticCompletionTest {
    @Property
    public void static_iterator_returns_all_candidates_given_list(@ForAll List<String> candidates) {
        val completion = new StaticCompletion(candidates) {};
        assertThat(completion).containsExactlyElementsOf(candidates);
    }

    @Property
    public void static_iterator_returns_all_candidates_given_array(@ForAll String[] candidates) {
        val completion = new StaticCompletion(candidates) {};
        assertThat(completion).containsExactlyElementsOf(List.of(candidates));
    }
}
