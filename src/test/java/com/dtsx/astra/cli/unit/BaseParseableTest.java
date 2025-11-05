package com.dtsx.astra.cli.unit;

import com.dtsx.astra.cli.core.datatypes.Either;
import lombok.RequiredArgsConstructor;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.CharRange;
import net.jqwik.api.constraints.NotBlank;

import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RequiredArgsConstructor
@SuppressWarnings("NewClassNamingConvention")
public abstract class BaseParseableTest {
    protected final String thing;
    protected final Function<String, Either<String, ?>> parser;

    @Provide
    protected Arbitrary<String> validString() {
        return Arbitraries.strings().excludeChars('\n', '\r', '<', '>', '\'', '"').ofMinLength(1).map(String::trim).filter(s -> s != null && !s.isBlank());
    }

    public static abstract class WithBasicValidation extends BaseParseableTest {
        public WithBasicValidation(String thing, Function<String, Either<String, ?>> parser) {
            super(thing, parser);
        }

        @Property(tries = 10)
        public void errors_if_blank_str(@ForAll @CharRange(from = ' ', to = ' ') String blank) {
            assertThat(parser.apply(quote(blank)))
                .extracting(Either::getLeft)
                .asString()
                .isEqualTo(thing + " should not be blank or empty");
        }

        @Property(tries = 10)
        public void errors_if_placeholder(@ForAll @NotBlank String value) {
            assertThat(parser.apply(quote("<" + value + ">")))
                .extracting(Either::getLeft)
                .asString()
                .isEqualTo(thing + " should not be enclosed in angle brackets... did you forget to replace a placeholder?");
        }

        @Example
        public void errors_on_invalid_chars(@ForAll @NotBlank @AlphaChars String value) {
            assertThat(parser.apply(quote(value + "\r\n" + value)))
                .extracting(Either::getLeft)
                .asString()
                .startsWith(thing + " contains the following invalid character(s): '")
                .contains("<carriage return>", "<newline>", ",")
                .endsWith("'");
        }

        protected String quote(String value) {
            return value;
        }
    }

    public static abstract class WithTrimAndBasicValidation extends WithBasicValidation {
        public WithTrimAndBasicValidation(String thing, Function<String, Either<String, ?>> parser) {
            super(thing, parser);
        }

        @Override
        protected String quote(String value) {
            return switch (value.length() % 3) {
                case 0 -> "\"" + value + "\"";
                case 1 -> "'" + value + "'";
                default -> value;
            };
        }
    }
}
