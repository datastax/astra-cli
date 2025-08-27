package com.dtsx.astra.cli.unit.utils;

import com.dtsx.astra.cli.testlib.laws.Idempotent;
import com.dtsx.astra.cli.utils.MiscUtils;
import lombok.val;
import net.jqwik.api.ForAll;
import net.jqwik.api.Group;
import net.jqwik.api.Property;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Group
public class MiscUtilsTest {
    @Group
    class setAdd implements Idempotent<Set<Object>> {
        @Property
        public <T> void adds_elem_to_returned_set(@ForAll Set<T> set, @ForAll T elem) {
            val newSet = MiscUtils.setAdd(set, elem);
            assertThat(newSet).hasSameElementsAs(new HashSet<>() {{ addAll(set); add(elem); }});
        }

        @Property
        public <T> void does_not_modify_original_set(@ForAll Set<T> set, @ForAll T elem) {
            val originalSet = new HashSet<>(set);
            MiscUtils.setAdd(set, elem);
            assertThat(set).hasSameElementsAs(originalSet);
        }

        @Override
        public Idempotent.Params<Set<Object>> idempotency() {
            return Idempotent.mkFn((init) -> (set) -> MiscUtils.setAdd(set, init));
        }
    }

    @Group
    class setDelete implements Idempotent<Set<Object>> {
        @Property
        public <T> void removes_elem_from_returned_set(@ForAll Set<T> set, @ForAll T elem) {
            val newSet = MiscUtils.setDel(set, elem);
            assertThat(newSet).hasSameElementsAs(new HashSet<>() {{ addAll(set); remove(elem); }});
        }

        @Property
        public <T> void does_not_modify_original_set(@ForAll Set<T> set, @ForAll T elem) {
            val originalSet = new HashSet<>(set);
            MiscUtils.setDel(set, elem);
            assertThat(set).hasSameElementsAs(originalSet);
        }

        @Override
        public Idempotent.Params<Set<Object>> idempotency() {
            return Idempotent.mkFn((init) -> (set) -> MiscUtils.setDel(set, init));
        }
    }
}
