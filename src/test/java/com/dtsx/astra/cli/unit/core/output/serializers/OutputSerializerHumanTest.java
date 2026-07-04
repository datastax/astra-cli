package com.dtsx.astra.cli.unit.core.output.serializers;

import com.dtsx.astra.cli.core.output.serializers.OutputSerializer;
import lombok.val;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Group;
import net.jqwik.api.Property;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

@Group
public class OutputSerializerHumanTest extends BaseOutputSerializerTest {
    @Override
    public Object serialize(Object o) {
        return OutputSerializer.serializeAsHuman(o);
    }

    @Override
    public Object emptyValue() {
        return "null";
    }

    @Override
    public Class<?>[] specialClasses() {
        return new Class[]{ Optional.class, Supplier.class, Collection.class, Enum.class };
    }

    @Group
    public class collections {
        @Example
        public void empty_collections_are_serialized_as_none() {
            assertThat(serialize(Set.of())).isEqualTo("<none>");
        }
    }

    @Group
    public class everything_else {
        @Property
        public <T> void everything_else_uses_toString(@ForAll T o) {
            assumeThat(o).isNotInstanceOfAny(specialClasses());
            assertThat(serialize(o)).isEqualTo(o.toString());
        }
    }
}
