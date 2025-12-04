package com.dtsx.astra.cli.unit.core.config;

import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.config.ProfileName;
import net.jqwik.api.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

@Group
public class ProfileTest {
    @Group
    public class isDefault {
        @Property
        public void false_if_profile_name_is_not_default(@ForAll Optional<ProfileName> profileName) {
            assumeThat(profileName).isNotEqualTo(Optional.of(ProfileName.DEFAULT));

            assertThat(
                new Profile(profileName, null, null, null).isDefault()
            ).isFalse();
        }

        @Example
        public void true_if_profile_name_is_default() {
            assertThat(
                new Profile(Optional.of(ProfileName.DEFAULT), null, null, null).isDefault()
            ).isTrue();
        }
    }

    @Group
    public class isReconstructedFromCreds {
        @Example
        public void true_if_profile_name_is_empty() {
            assertThat(
                new Profile(Optional.empty(), null, null, null).isReconstructedFromCreds()
            ).isTrue();
        }

        @Property
        public void false_if_profile_name_is_present(@ForAll ProfileName profileName) {
            assertThat(
                new Profile(Optional.of(profileName), null, null, null).isReconstructedFromCreds()
            ).isFalse();
        }
    }

    @Group
    public class nameOrDefault {
        @Example
        public void returns_default_name_if_empty() {
            assertThat(
                new Profile(Optional.empty(), null, null, null).nameOrDefault()
            ).isEqualTo(ProfileName.mkUnsafe("<args_provided>"));
        }

        @Property
        public void returns_name_if_present(@ForAll ProfileName profileName) {
            assertThat(
                new Profile(Optional.of(profileName), null, null, null).nameOrDefault()
            ).isEqualTo(profileName);
        }
    }
}
