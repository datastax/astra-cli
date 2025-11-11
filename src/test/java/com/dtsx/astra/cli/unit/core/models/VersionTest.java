package com.dtsx.astra.cli.unit.core.models;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.Version;
import com.dtsx.astra.cli.unit.BaseParseableTest;
import lombok.val;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Group
public class VersionTest extends BaseParseableTest.WithTrimAndBasicValidation {
    public VersionTest() {
        super("Version", Version::parse);
    }

    @Group
    public class creation {
        @Property
        public void creates_version_from_any_valid_string(@ForAll("version") String validName) {
            assertThat(parser.apply(validName))
                .extracting(Either::getRight)
                .extracting(Object::toString)
                .isEqualTo(validName.replaceFirst("^v", "").toLowerCase());
        }

        @Property
        public void mk_unsafe_creates_version_from_any_valid_string(@ForAll("version") String validName) {
            val version = Version.mkUnsafe(validName);
            assertThat(version.toString()).isEqualTo(validName.replaceFirst("^v", "").toLowerCase());
        }

        @Property
        public void mk_under_equals_parse_get_right(@ForAll String str) {
            val fromParse = parser.apply(str);

            fromParse.fold(
                _ -> {
                    return assertThatThrownBy(() -> Version.mkUnsafe(str)).isInstanceOf(RuntimeException.class);
                },
                ver -> {
                    val fromMkUnsafe = Version.mkUnsafe(str);
                    return assertThat(fromMkUnsafe).isEqualTo(ver);
                }
            );
        }
    }

    @Group
    public class comparison {
        @Example
        public void compares_versions_correctly() {
            gt("1.0.0", "0.9.9");
            gt("1.0.0", "1.0.0-alpha.1");
            gt("1.0.0-beta.1", "1.0.0-alpha.1");
            gt("1.0.0-beta.2", "1.0.0-beta.1");
            gt("1.0.0-rc.1", "1.0.0-beta.5");
            gt("1.0.1", "1.0.0-rc.1");
            gt("1.1.0", "1.0.9");
            gt("2.0.0", "1.9.9");

            eq("1.0.0", "1.0.0");
            eq("1.0.0-alpha.1", "1.0.0-alpha.1");
            eq("1.0.0-BETA.1", "1.0.0-beta.1");
        }

        private void gt(String v1, String v2) {
            assertThat(Version.mkUnsafe(v1)).isGreaterThan(Version.mkUnsafe(v2));
        }

        private void eq(String v1, String v2) {
            assertThat(Version.mkUnsafe(v1)).isEqualTo(Version.mkUnsafe(v2));
        }
    }

    @Group
    public class short_version_format {
        @Example
        public void parses_two_part_versions_as_zero_patch() {
            assertThat(Version.mkUnsafe("0.6").toString()).isEqualTo("0.6.0");
            assertThat(Version.mkUnsafe("1.2").toString()).isEqualTo("1.2.0");
            assertThat(Version.mkUnsafe("v1.5").toString()).isEqualTo("1.5.0");
        }

        @Example
        public void compares_short_versions_correctly() {
            assertThat(Version.mkUnsafe("0.6")).isEqualTo(Version.mkUnsafe("0.6.0"));
            assertThat(Version.mkUnsafe("1.2")).isEqualTo(Version.mkUnsafe("1.2.0"));
            assertThat(Version.mkUnsafe("1.2")).isGreaterThan(Version.mkUnsafe("1.1.0"));
            assertThat(Version.mkUnsafe("1.2")).isGreaterThan(Version.mkUnsafe("1.1"));
        }
    }

    @Provide
    private Arbitrary<String> version() {
        return Arbitraries.integers().greaterOrEqual(0).list().ofSize(4).flatMap((nums) -> {
            val major = nums.get(0);
            val minor = nums.get(1);
            val patch = nums.get(2);

            return Arbitraries.strings().alpha().ofMinLength(1).optional().map((label) -> {
                var version = String.format("%d.%d.%d", major, minor, patch);

                if (major % 2 == 0) {
                    version = "v" + version;
                }

                if (label.isPresent()) {
                    version += "-" + label.get() + ".1";
                }

                return version;
            });
        });
    }
}
