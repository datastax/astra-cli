package com.dtsx.astra.cli.unit.core.datatypes;

import com.dtsx.astra.cli.core.datatypes.Ref;
import lombok.val;
import net.jqwik.api.Assume;
import net.jqwik.api.ForAll;
import net.jqwik.api.Group;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.WithNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@Group
public class RefTest {
    @Group
    public class factories {
        @Property
        public <T> void constructor_evaluates_function(@ForAll @WithNull T value) {
            val ref = new Ref<T>((_) -> value);
            assertThat(ref.get()).isEqualTo(value);
        }

        @Property
        public <T> void constructor_passes_getter_as_function(@ForAll @WithNull T initial, @ForAll @WithNull T updated) {
            val getter = new Object() {
                Supplier<T> ref;
            };

            val ref = new Ref<T>((_getter) -> {
                getter.ref = _getter;
                return initial;
            });

            assertThat(getter.ref).isNotNull();
            assertThat(getter.ref.get()).isEqualTo(ref.get()).isEqualTo(initial);

            ref.modify((_) -> updated);

            assertThat(getter.ref.get()).isEqualTo(ref.get()).isEqualTo(updated);
        }
    }

    @Group
    public class getAndSet {
        @Property
        public <T> void work_as_expected(@ForAll List<@WithNull T> values) {
            Assume.that(!values.isEmpty());
            
            val ref = new Ref<>(_ -> values.getFirst());

            for (val value : values) {
                ref.modify((_) -> value);
                assertThat(ref.get()).isEqualTo(value);
            }

            assertThat(ref.get()).isEqualTo(values.getLast());
        }
    }

    @Group
    public class listeners {
        @Property
        public <T> void onUpdate_adds_listener(@ForAll T initialValue, @ForAll T newValue) {
            val ref = new Ref<T>(_ -> initialValue);
            val notifiedValues = new ArrayList<T>();

            ref.onUpdate(notifiedValues::addLast);
            ref.modify((_) -> newValue);

            assertThat(notifiedValues).containsExactly(newValue);
        }

        @Property
        public <T> void multiple_listeners_all_called(@ForAll T initialValue, @ForAll T newValue) {
            val ref = new Ref<T>(_ -> initialValue);
            val listener1Values = new ArrayList<T>();
            val listener2Values = new ArrayList<T>();
            val listener3Values = new ArrayList<T>();

            ref.onUpdate(listener1Values::addLast);
            ref.onUpdate(listener2Values::addLast);
            ref.onUpdate(listener3Values::addLast);

            ref.modify((_) -> newValue);

            assertThat(listener1Values).containsExactly(newValue);
            assertThat(listener2Values).containsExactly(newValue);
            assertThat(listener3Values).containsExactly(newValue);
        }

        @Property
        public <T> void listeners_called_in_order_of_registration(@ForAll T initialValue, @ForAll T newValue) {
            val ref = new Ref<>(_ -> initialValue);
            val callOrder = new ArrayList<String>();

            ref.onUpdate(_ -> callOrder.add("first"));
            ref.onUpdate(_ -> callOrder.add("second"));
            ref.onUpdate(_ -> callOrder.add("third"));

            ref.modify((_) -> newValue);

            assertThat(callOrder).containsExactly("first", "second", "third");
        }

        @Property
        public <T> void listeners_called_for_each_set(@ForAll T initialValue, @ForAll List<T> updates) {
            Assume.that(!updates.isEmpty());
            
            val ref = new Ref<T>(_ -> initialValue);
            val notifiedValues = new ArrayList<T>();

            ref.onUpdate(notifiedValues::addLast);

            for (val update : updates) {
                ref.modify((_) -> update);
            }

            assertThat(notifiedValues).containsExactlyElementsOf(updates);
        }
    }
}
