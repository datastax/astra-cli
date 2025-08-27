package com.dtsx.astra.cli.unit.utils;

import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.testlib.laws.Idempotent;
import com.dtsx.astra.cli.utils.StringUtils;
import lombok.val;
import net.jqwik.api.*;
import org.graalvm.collections.Pair;

import java.util.List;
import java.util.stream.IntStream;

import static com.dtsx.astra.cli.utils.StringUtils.NL;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

@Group
public class StringUtilsTest {
    @Group
    class removeQuotesIfAny implements Idempotent<String> {
        @Property
        public void removes_quotes_when_present(@ForAll String unquoted, @ForAll boolean singleQuotes) {
            val quoted = (singleQuotes) ? "'" + unquoted + "'" : '"' + unquoted + '"';

            val result = StringUtils.removeQuotesIfAny(quoted);

            assertThat(result).isEqualTo(unquoted);
        }

        @Property
        public void doesnt_trim_mismatched_quotes(@ForAll String unquoted, @ForAll boolean orientation) {
            val quoted = (orientation) ? "'" + unquoted + '"' : '"' + unquoted + "'";

            val result = StringUtils.removeQuotesIfAny(quoted);

            assertThat(result).isEqualTo(quoted);
        }

        @Property
        public void doesnt_trim_when_no_quotes(@ForAll String string) {
            Assume.that(!(string.startsWith("'") && string.endsWith("'")) && !(string.startsWith("\"") && string.endsWith("\"")));

            val result = StringUtils.removeQuotesIfAny(string);

            assertThat(result).isEqualTo(string);
        }

        @Override
        public Idempotent.Params<String> idempotency() {
            return Idempotent.fn(StringUtils::removeQuotesIfAny);
        }
    }

    @Group
    class withIndent implements Idempotent<String> {
        @Property
        public void adds_indent_from_min_initial_indent(@ForAll("intendedStrings") Pair<String, List<Pair<String, Integer>>> test) {
            val targetIndent = test.getLeft();
            val initialLines = test.getRight();

            val expectedLines = IntStream.range(0, initialLines.size())
                .filter(i -> !(i == 0 || i == initialLines.size() - 1) || !initialLines.get(i).getLeft().isBlank())
                .mapToObj(initialLines::get)
                .toList();

            val minIndent = expectedLines.stream()
                .filter(l -> !l.getLeft().isBlank())
                .mapToInt(Pair::getRight)
                .min()
                .orElse(0);

            val initialStr = initialLines.stream()
                .map((p) -> " ".repeat(p.getRight()) + p.getLeft())
                .collect(joining(NL));

            val result = StringUtils.withIndent(initialStr, targetIndent);

            val expectedStr = expectedLines.stream()
                .map((p) -> targetIndent + " ".repeat(Math.max(p.getRight() - minIndent, 0)) + p.getLeft())
                .collect(joining(NL));

            assertThat(result).isEqualTo(expectedStr);
        }

        @Provide
        private Arbitrary<Pair<String, List<Pair<String, Integer>>>> intendedStrings() {
            return Arbitraries.strings().excludeChars('\n').flatMap((targetIndent) -> {
                val arb = Arbitraries.strings().excludeChars('\n').list()
                    .flatMapEach((_, elem) -> {
                        return Arbitraries.integers().between(0, 50).map((initialIndent) -> Pair.create(elem.trim(), initialIndent));
                    });

                return arb.map((initialLines) -> Pair.create(targetIndent, initialLines));
            });
        }

        @Override
        public Idempotent.Params<String> idempotency() {
            return Idempotent.mkFn((init) -> (str) -> StringUtils.withIndent(str, init.length() % 10));
        }
    }

    @Group
    class maskToken {
        @Property
        public void operation_defers_to_astra_token_class(@ForAll AstraToken token) {
            val result = StringUtils.maskToken(token.unwrap());
            
            assertThat(result).isNotNull();
        }

        @Property
        public void invalid_tokens_are_masked_as_invalid(@ForAll String invalidToken) {
            Assume.that(AstraToken.parse(invalidToken).isLeft());

            val result = StringUtils.maskToken(invalidToken);

            assertThat(result).matches(".*<invalid_token\\('.*?'\\)>.*");
        }
    }

    @Group
    class isPositiveInteger {
        @Property
        public void returns_true_for_int_strings(@ForAll int i) {
            assumeThat(i).isPositive();

            assertThat(StringUtils.isPositiveInteger(String.valueOf(i))).isTrue();
        }

        @Property
        public void returns_false_for_non_int_strings(@ForAll String str) {
            assumeThat(str).doesNotMatch("\\d+");
            
            assertThat(StringUtils.isPositiveInteger(str)).isFalse();
        }
    }

    @Group
    class truncate {
        @Property
        public void short_strings_unchanged(@ForAll("underLength") Pair<String, Integer> pair) {
            val str = pair.getLeft();
            val maxLength = pair.getRight();

            assertThat(StringUtils.truncate(str, maxLength)).isEqualTo(str);
        }

        @Property
        public void long_strings_get_truncated(@ForAll("overLength") Pair<String, Integer> pair) {
            val str = pair.getLeft();
            val maxLength = pair.getRight();
            val lastIndex = Math.max(maxLength - 1, 0);

            val result = StringUtils.truncate(str, maxLength);

            assertThat(result).hasSize(maxLength);
            assertThat(result.substring(0, lastIndex)).isEqualTo(str.substring(0, lastIndex));

            if (maxLength > 0) {
                assertThat(result.charAt(lastIndex)).isEqualTo('…');
            } else {
                assertThat(result).isEmpty();
            }
        }

        @Provide
        private Arbitrary<Pair<String, Integer>> underLength() {
            return Arbitraries.integers().between(0, 100).flatMap((maxLen) -> Arbitraries.strings().ofMaxLength(maxLen).map((str) -> Pair.create(str, maxLen)));
        }

        @Provide
        private Arbitrary<Pair<String, Integer>> overLength() {
            return Arbitraries.integers().between(0, 100).flatMap((maxLen) -> Arbitraries.strings().ofMinLength(maxLen + 1).map((str) -> Pair.create(str, maxLen)));
        }
    }

    @Group
    class capitalize implements Idempotent<String> {
        @Property
        public void capitalizes(@ForAll String str) {
            val result = StringUtils.capitalize(str);

            if (str.isEmpty()) {
                assertThat(result).isEmpty();
            } else {
                assertThat(result.charAt(0)).isEqualTo(Character.toUpperCase(str.charAt(0)));
                assertThat(result.substring(1)).isEqualTo(str.substring(1));
            }
        }

        @Override
        public Idempotent.Params<String> idempotency() {
            return Idempotent.fn(StringUtils::removeQuotesIfAny);
        }
    }
}
