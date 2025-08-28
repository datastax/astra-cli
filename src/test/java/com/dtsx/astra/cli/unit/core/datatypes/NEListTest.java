package com.dtsx.astra.cli.unit.core.datatypes;

import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.testlib.laws.Functor;
import lombok.val;
import net.jqwik.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

class NEListTest implements Functor<NEList<Object>> {
    @Group
    public class parse {
        @Property
        public <T> void returns_empty_for_empty_collection(@ForAll("collections") Collection<T> col) {
            col.clear();
            assertThat(NEList.parse(col)).isEmpty();
        }

        @Property
        public <T> void returns_non_empty_for_non_empty_collection(@ForAll("collections") Collection<T> col) {
            assumeThat(col).isNotEmpty();

            val neList = NEList.parse(col);

            assertThat(neList).isNotEmpty();
            assertThat(neList.get()).isInstanceOf(NEList.class);
            assertThat(neList.get()).containsExactlyElementsOf(col);
        }

        @Provide
        private Arbitrary<Collection<?>> collections() {
            return Arbitraries.defaultFor(Object.class).list().flatMap((elems) -> Arbitraries.oneOf(
                Arbitraries.just(new ArrayList<>(elems)),
                Arbitraries.just(new LinkedList<>(elems)),
                Arbitraries.just(new HashSet<>(elems)),
                Arbitraries.just(new ArrayDeque<>(elems)),
                Arbitraries.just(new TreeSet<>()),
                Arbitraries.just(Collections.checkedList(elems, Object.class)),
                Arbitraries.just(Collections.synchronizedList(new ArrayList<>(elems)))
            ));
        }
    }

    @Override
    public Params<NEList<Object>> functor() {
        return Functor.params(NEList::of, NEList::map);
    }
}
