package com.dtsx.astra.cli.unit.core.config;

import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.unit.BaseParseableTest;
import com.dtsx.astra.cli.utils.JsonUtils;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

@Group
public class ProfileNameTest extends BaseParseableTest.WithTrimAndBasicValidation {
    public ProfileNameTest() {
        super("Profile name", ProfileName::parse);
    }

    @Group
    public class creation {
        @Property
        public void creates_profile_name_from_any_valid_string(@ForAll("validString") String validName) {
            assertThat(parser.apply(validName))
                .extracting(Either::getRight)
                .isEqualTo(ProfileName.mkUnsafe(validName.trim()));
        }

        @Property
        public void mk_unsafe_works_with_any_string(@ForAll String name) {
            ProfileName profileName = ProfileName.mkUnsafe(name);
            assertThat(profileName.unwrap()).isEqualTo(name);
        }
    }

    @Group
    public class isDefault {
        @Property
        public void returns_false_if_not_default(@ForAll String name) {
            assumeThat(name).isNotEqualTo("default");
            assertThat(ProfileName.mkUnsafe(name).isDefault()).isFalse();
        }

        @Example
        public void returns_true_if_default() {
            assertThat(ProfileName.mkUnsafe("default").isDefault()).isTrue();
        }
    }

    @Group
    public class toJSON {
        @Property
        public void returns_unwrapped_string(@ForAll String name) {
            assertThat(JsonUtils.writeValue(ProfileName.mkUnsafe(name)))
                .isEqualTo(JsonUtils.writeValue(name));
        }
    }
}
