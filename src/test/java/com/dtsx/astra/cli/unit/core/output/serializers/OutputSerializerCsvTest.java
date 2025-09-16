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
public class OutputSerializerCsvTest extends BaseOutputSerializerTest {
    @Override
    public Object serialize(Object o) {
        return OutputSerializer.serializeAsCsv(o);
    }

    @Override
    public Object emptyValue() {
        return "";
    }

    @Override
    public Class<?>[] specialClasses() {
        return new Class[]{ Optional.class, Supplier.class, Collection.class, Enum.class };
    }

    @Group
    public class collections {
        @Example
        public void collections_are_serialized_as_csvs_in_quotes() {
            val data = List.of("\"a\"", Optional.of("\"b\"\nc"), (Supplier<String>) () -> "d");

            val expected = trimIndent("""
            \"""a"",""b""
            c,d"
            """);

            assertThat(serialize(data)).isEqualTo(expected);
        }

        @Property
        public <T> void singleton_collections_are_just_unwrapped(@ForAll  T t) {
            assertThat(serialize(Set.of(t))).isEqualTo(serialize(t));
        }

        @Example
        public void empty_collections_are_serialized_as_empty_string() {
            assertThat(serialize(Set.of())).isEqualTo("");
        }
    }

    @Group
    public class everything_else {
        @Property
        public <T> void everything_else_uses_toString_with_special_chars_quoted(@ForAll T o) {
            assumeThat(o).isNotInstanceOfAny(specialClasses());

            val asString = o.toString();

            if (asString.contains(",") || asString.contains("\"") || asString.contains("\n")) {
                assertThat(OutputSerializer.serializeAsCsv(o)).isEqualTo('"' + asString.replace("\"", "\"\"") + '"');
            } else {
                assertThat(OutputSerializer.serializeAsCsv(o)).isEqualTo(asString);
            }
        }

        @Example
        public void strings_with_special_chars_are_quoted() {
            assertThat(serialize("a,b")).isEqualTo("\"a,b\"");

            assertThat(serialize("a\"b")).isEqualTo("\"a\"\"b\"");

            assertThat(serialize("a\nb")).isEqualTo("\"a\nb\"");

            assertThat(serialize("a\"b,c\nd")).isEqualTo("\"a\"\"b,c\nd\"");
        }
    }
}
