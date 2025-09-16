package com.dtsx.astra.cli.unit.core.output.serializers;

import com.dtsx.astra.cli.core.output.serializers.OutputSerializer;
import net.jqwik.api.ForAll;
import net.jqwik.api.Group;
import net.jqwik.api.Property;

import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

@Group
public class OutputSerializerJsonTest extends BaseOutputSerializerTest {
    @Override
    public Object serialize(Object o) {
        return OutputSerializer.serializeAsJson(o);
    }

    @Override
    public Object emptyValue() {
        return null;
    }

    @Override
    public Class<?>[] specialClasses() {
        return new Class[]{ Optional.class, Supplier.class, Enum.class };
    }

    @Group
    public class everything_else {
        @Property
        public <T> void everything_else_is_serialized_as_is(@ForAll T o) {
            assumeThat(o).isNotInstanceOfAny(specialClasses());
            assertThat(serialize(o)).isEqualTo(o);
        }
    }
}
