package com.dtsx.astra.cli.unit.core.output.serializers;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Group;
import net.jqwik.api.Property;

import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseOutputSerializerTest {
    public abstract Object serialize(Object o);

    public abstract Object emptyValue();
    public abstract Class<?>[] specialClasses();

    @Group
    public class optional {
        @Property
        public <T> void optionals_are_unwrapped_and_serialized(@ForAll T o) {
            assertThat(serialize(Optional.of(o))).isEqualTo(serialize(o));
        }

        @Example
        public void empty_optionals_are_serialized_as_empty_string() {
            assertThat(serialize(Optional.empty())).isEqualTo(emptyValue());
        }
    }

    @Group
    public class supplier {
        @Property
        public <T> void suppliers_are_evaluated_and_serialized(@ForAll T o) {
            assertThat(serialize((Supplier<T>) () -> o)).isEqualTo(serialize(o));
        }
    }

    @Group
    public class enums {
        @Property
        public void enums_are_serialized_using_their_name(@ForAll Enum<?> e) {
            assertThat(serialize(e)).isEqualTo(e.name());
        }
    }
}
