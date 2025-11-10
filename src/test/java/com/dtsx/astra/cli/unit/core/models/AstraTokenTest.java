package com.dtsx.astra.cli.unit.core.models;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.testlib.Fixtures;
import com.dtsx.astra.cli.unit.BaseParseableTest;
import lombok.val;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Group;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.CharRange;
import net.jqwik.api.constraints.NotBlank;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

public class AstraTokenTest extends BaseParseableTest.WithTrimAndBasicValidation {
    public AstraTokenTest() {
        super("Astra token", AstraToken::parse);
    }

    @Group
    public class creation {
        @Example
        public void errors_if_token_like_json() {
            for (val str : List.of("{}", "[]", "{ \"token\": \"" + Fixtures.Token.unsafeUnwrap() + "\" }")) {
                assertThat((parser.apply(str)))
                    .extracting(Either::getLeft)
                    .isEqualTo("Astra token should not be passed as JSON; it should be a plain string");
            }
        }

        @Property
        public void errors_if_token_looks_like_filename(@ForAll @AlphaChars String name, @ForAll @NotBlank @CharRange(from = 'a', to = 'z') String ext) {
            assertThat((parser.apply(name + "." + ext)))
                .extracting(Either::getLeft)
                .isEqualTo("Astra token looks like a file name; use the @file syntax to pass the token from a file, where the file contains only the token as a plain string (e.g. `--token @token.txt`)");
        }

        @Property
        public void errors_if_token_does_not_start_with_astra_cs(@ForAll @NotBlank @AlphaChars String token) {
            assumeThat(token).doesNotStartWith("AstraCS:");

            assertThat((parser.apply(token)))
                .extracting(Either::getLeft)
                .isEqualTo("Astra token should start with 'AstraCS:'");
        }

        @Property
        public void errors_if_token_length_invalid(@ForAll @AlphaChars String token) {
            token = "AstraCS:" + token;

            assumeThat(token.length()).isNotEqualTo(97);

            assertThat((parser.apply(token)))
                .extracting(Either::getLeft)
                .isEqualTo("Astra token should be exactly 97 characters long; yours is " + token.length());
        }
    }

    @Property
    public void to_string_does_not_leak_token(@ForAll AstraToken token) {
        assertThat(token.toString())
            .hasSize("AstraCS:".length() + 12)
            .contains("****");
    }
}
