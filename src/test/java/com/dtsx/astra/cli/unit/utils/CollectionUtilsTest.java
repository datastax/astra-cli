package com.dtsx.astra.cli.unit.utils;

import com.dtsx.astra.cli.testlib.laws.Idempotent;
import com.dtsx.astra.cli.utils.CollectionUtils;
import lombok.val;
import net.jqwik.api.ForAll;
import net.jqwik.api.Group;
import net.jqwik.api.Property;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Group
public class CollectionUtilsTest {
    @Group
    class setAdd implements Idempotent<Set<Object>> {
        @Property
        public <T> void adds_elem_to_returned_set(@ForAll Set<T> set, @ForAll T elem) {
            val newSet = CollectionUtils.setAdd(set, elem);
            assertThat(newSet).hasSameElementsAs(new HashSet<>() {{ addAll(set); add(elem); }});
        }

        @Property
        public <T> void does_not_modify_original_set(@ForAll Set<T> set, @ForAll T elem) {
            val originalSet = new HashSet<>(set);
            CollectionUtils.setAdd(set, elem);
            assertThat(set).hasSameElementsAs(originalSet);
        }

        @Override
        public Idempotent.Params<Set<Object>> idempotency() {
            return Idempotent.mkFn((init) -> (set) -> CollectionUtils.setAdd(set, init));
        }
    }

    @Group
    class setDelete implements Idempotent<Set<Object>> {
        @Property
        public <T> void removes_elem_from_returned_set(@ForAll Set<T> set, @ForAll T elem) {
            val newSet = CollectionUtils.setDel(set, elem);
            assertThat(newSet).hasSameElementsAs(new HashSet<>() {{ addAll(set); remove(elem); }});
        }

        @Property
        public <T> void does_not_modify_original_set(@ForAll Set<T> set, @ForAll T elem) {
            val originalSet = new HashSet<>(set);
            CollectionUtils.setDel(set, elem);
            assertThat(set).hasSameElementsAs(originalSet);
        }

        @Override
        public Idempotent.Params<Set<Object>> idempotency() {
            return Idempotent.mkFn((init) -> (set) -> CollectionUtils.setDel(set, init));
        }
    }

    @Group
    class listAdd {
        @Property
        public <T> void adds_elem_to_end_of_returned_list(@ForAll List<T> list, @ForAll T elem) {
            val newList = CollectionUtils.listAdd(list, elem);
            val expected = new ArrayList<>(list);
            expected.add(elem);
            assertThat(newList).isEqualTo(expected);
        }

        @Property
        public <T> void adds_elem_to_beginning_of_returned_list(@ForAll List<T> list, @ForAll T elem) {
            val newList = CollectionUtils.listAdd(elem, list);
            val expected = new ArrayList<T>();
            expected.add(elem);
            expected.addAll(list);
            assertThat(newList).isEqualTo(expected);
        }

        @Property
        public <T> void does_not_modify_original_list(@ForAll List<T> list, @ForAll T elem) {
            val originalList = new ArrayList<>(list);
            CollectionUtils.listAdd(list, elem);
            CollectionUtils.listAdd(elem, list);
            assertThat(list).isEqualTo(originalList);
        }
    }

    @Group
    class listConcat {
        @Property
        public <T> void concatenates_lists_in_order(@ForAll List<T> list1, @ForAll List<T> list2) {
            val newList = CollectionUtils.listConcat(list1, list2);
            val expected = new ArrayList<>(list1);
            expected.addAll(list2);
            assertThat(newList).isEqualTo(expected);
        }

        @Property
        public <T> void does_not_modify_original_lists(@ForAll List<T> list1, @ForAll List<T> list2) {
            val original1 = new ArrayList<>(list1);
            val original2 = new ArrayList<>(list2);
            CollectionUtils.listConcat(list1, list2);
            assertThat(list1).isEqualTo(original1);
            assertThat(list2).isEqualTo(original2);
        }
    }
}
