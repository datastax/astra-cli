package com.dtsx.astra.cli.testlib;

import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.utils.JsonUtils;
import lombok.Cleanup;
import lombok.val;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ObjectAssert;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class AssertUtils {
    public static AbstractBooleanAssert<?> assertTrue(boolean condition) {
        return assertThat(condition).isTrue();
    }

    public static <T> ObjectAssert<T> assertEquals(T actual, T expected) {
        return assertThat(actual).isEqualTo(expected);
    }

    public static <T> T assertNonNull(T t) {
        return assertThat(t).isNotNull().actual();
    }

    public static <T, R> R assertNotCalled(T t) {
        throw new AssertionError("This function should not have been called");
    }

    public static <T extends CharSequence> T assertIsValidJsonOutput(T cs) {
        val asString = cs.toString();

        if (asString.isBlank()) {
            return Assertions.fail("Output is blank");
        }

        try {
            val node = JsonUtils.objectMapper().readTree(asString);

            if (!node.isObject()) {
                return Assertions.fail("String is not a valid JSON object (is a " + node.getNodeType() + ")");
            }

            var codeFound = false;

            for (var it = node.fields(); it.hasNext(); ) {
                val e = it.next();

                switch (e.getKey()) {
                    case OutputJson.Fields.CODE -> {
                        codeFound = true;

                        if (!isInEnum(e.getValue().asText(), ExitCode.class)) {
                            return Assertions.fail("code field is not a valid ExitCode enum value: " + e.getValue().toString());
                        }
                    }
                    case OutputJson.Fields.MESSAGE -> {
                        if (!e.getValue().isTextual()) {
                            return Assertions.fail("message field is not a valid string (is a " + e.getValue().getNodeType() + "): " + e.getValue().toString());
                        }
                    }
                    case OutputJson.Fields.DATA -> {
                        // no assertions yet
                    }
                    case OutputJson.Fields.NEXT_STEPS -> {
                        if (!e.getValue().isArray() && !e.getValue().isNull()) {
                            return Assertions.fail("nextSteps field is not a valid JSON array or null (is a " + e.getValue().getNodeType() + "): " + e.getValue().toString());
                        }
                    }
                }
            }

            if (!codeFound) {
                return Assertions.fail("code field is missing");
            }

            return cs;
        } catch (Exception e) {
            return Assertions.fail("Not a valid JSON output: " + e.getMessage());
        }
    }

    public static <T extends CharSequence> T assertIsValidCsvOutput(T cs) {
        val asString = cs.toString();

        if (asString.isBlank()) {
            return Assertions.fail("Output is blank");
        }

        try {
            @Cleanup val parser = CSVParser.parse(asString, CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).get());

            val headersStr = String.join(",", parser.getHeaderNames());

            if (!headersStr.matches("^code,message(,(?!code$|message$)[a-z0-9_]+)*$")) {
                return Assertions.fail("Invalid CSV headers: " + headersStr);
            }

            return cs;
        } catch (Exception e) {
            return Assertions.fail("Not a valid CSV output: " + e.getMessage());
        }
    }

    public static <T extends CharSequence, E extends Enum<E>> T assertIsInEnum(T cs, Class<E> enumClass) {
        return assertThat(cs).is(new Condition<>((s) -> isInEnum(s, enumClass), "is in enum " + enumClass.getSimpleName())).actual();
    }

    private static <E extends Enum<E>> boolean isInEnum(CharSequence value, Class<E> enumClass) {
        try {
            Enum.valueOf(enumClass, value.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
