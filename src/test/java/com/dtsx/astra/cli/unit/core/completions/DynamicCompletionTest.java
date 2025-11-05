package com.dtsx.astra.cli.unit.core.completions;

import com.dtsx.astra.cli.core.completions.DynamicCompletion;
import lombok.val;
import net.jqwik.api.ForAll;
import net.jqwik.api.Group;
import net.jqwik.api.Property;

import static org.assertj.core.api.Assertions.assertThat;

@Group
public class DynamicCompletionTest {
    @Property
    public void dynamic_iterator_returns_class_name_with_marker(@ForAll String str) {
        val completion = new DynamicCompletion(str) {};

        assertThat(completion).containsExactly(DynamicCompletion.marker(completion));

        assertThat(completion).first().isEqualTo("!$!dynamic-completion!$!:" + completion.getClass().getName());
    }
}
