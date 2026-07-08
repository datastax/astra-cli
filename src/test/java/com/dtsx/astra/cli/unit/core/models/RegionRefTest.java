package com.dtsx.astra.cli.unit.core.models;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionRef;
import com.dtsx.astra.cli.unit.BaseParseableTest;
import lombok.val;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Group;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.NotBlank;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class RegionRefTest extends BaseParseableTest.WithTrimAndBasicValidation {
    private static final UUID SAMPLE_ID = UUID.fromString("822b0fff-6a73-4322-a8ec-09832b075287");

    public RegionRefTest() {
        super("Region", RegionRef::parse);
    }

    @Group
    public class region_fallback {
        @Property
        public void uses_given_region_over_db_ref_region(@ForAll @NotBlank @AlphaChars String dbRegion, @ForAll @NotBlank @AlphaChars String givenRegion) {
            val result = RegionRef.parse(dbRef(dbRegion), Optional.of(givenRegion));
            assertThat(result.isRight()).isTrue();
            assertThat(result.getRight().isPresent()).isTrue();
            assertThat(result.getRight().get().toString()).isEqualTo(givenRegion);
        }

        @Property
        public void uses_db_ref_region_when_given_region_is_empty(@ForAll @NotBlank @AlphaChars String dbRegion) {
            val result = RegionRef.parse(dbRef(dbRegion), Optional.empty());
            if (result.isLeft()) {
                System.out.println("Error: " + result.getLeft());
            }
            assertThat(result.getRight().isPresent()).isTrue();
            assertThat(result.getRight().get().toString()).isEqualTo(dbRegion);
        }

        @Example
        public void returns_empty_when_no_region_is_given_and_db_ref_is_null() {
            val result = RegionRef.parse(null, Optional.empty());
            assertThat(result.getRight().isPresent()).isFalse();
        }

        @Example
        public void returns_empty_when_no_region_is_given_and_db_ref_has_no_region() {
            val result = RegionRef.parse(dbRef(), Optional.empty());
            assertThat(result.getRight().isPresent()).isFalse();
        }

        @Example
        public void returns_error_when_given_region_is_blank() {
            val result = RegionRef.parse(dbRef(), Optional.of("   "));
            assertThat(result.getLeft()).contains("Region should not be blank or empty");
        }
    }

    private DbRef dbRef() {
        return DbRef.fromId(SAMPLE_ID);
    }

    private DbRef dbRef(String region) {
        return DbRef.parse("https://" + SAMPLE_ID + "-" + region + ".apps.astra.datastax.com/api/json/v1").getRight();
    }
}
