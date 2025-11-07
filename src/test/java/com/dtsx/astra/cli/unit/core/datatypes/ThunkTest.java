package com.dtsx.astra.cli.unit.core.datatypes;

import com.dtsx.astra.cli.core.datatypes.Thunk;
import lombok.val;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class ThunkTest {
    @Property
    public <T> void thunk_caches_value(@ForAll T value) {
        val ref = new Object() { int calls = 0; };

        Supplier<T> supplier = () -> {
            ref.calls++;
            return value;
        };

        val thunk = new Thunk<>(supplier);

        val first = thunk.get();

        for (var i = 0; i < 10; i++) {
            val v = thunk.get();
            assertThat(v).isEqualTo(first);
        }

        assertThat(first).isEqualTo(value);
        assertThat(ref.calls).isEqualTo(1);
    }
}
