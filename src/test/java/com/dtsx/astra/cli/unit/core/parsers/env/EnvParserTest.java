package com.dtsx.astra.cli.unit.core.parsers.env;

import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.parsers.env.EnvParseException;
import com.dtsx.astra.cli.core.parsers.env.EnvParser;
import com.dtsx.astra.cli.core.parsers.env.ast.EnvComment;
import com.dtsx.astra.cli.core.parsers.env.ast.EnvEmptyLine;
import com.dtsx.astra.cli.core.parsers.env.ast.EnvKVPair;
import com.dtsx.astra.cli.core.parsers.env.ast.EnvNode;
import lombok.val;
import org.junit.jupiter.api.Test;
import picocli.CommandLine.Help.Ansi;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EnvParserTest {
    private static final AstraColors NO_COLORS = new AstraColors(Ansi.OFF);

    private void assertRoundTrip(String input, EnvNode... expectedNodes) throws EnvParseException {
        val envFile = new EnvParser().parseEnvFile(new Scanner(input));
        assertThat(envFile.nodes()).isEqualTo(List.of(expectedNodes));
        assertThat(envFile.render(NO_COLORS).trim()).isEqualTo(input.trim());
    }

    @Test
    public void simple_key_value_pairs() throws EnvParseException {
        assertRoundTrip(
            """
            KEY1=value 1
            KEY2="value 2"
            KEY3='value 3'
            """,
            new EnvKVPair("KEY1", "value 1", Optional.empty()),
            new EnvKVPair("KEY2", "\"value 2\"", Optional.empty()),
            new EnvKVPair("KEY3", "'value 3'", Optional.empty())
        );
    }

    @Test
    public void quoted_value_with_escaped_quote() throws EnvParseException {
        assertRoundTrip(
            "KEY=\"val\\\"ue\"",
            new EnvKVPair("KEY", "\"val\\\"ue\"", Optional.empty())
        );
    }

    @Test
    public void hash_inside_quotes_is_not_a_comment() throws EnvParseException {
        assertRoundTrip(
            """
            KEY="value#1"
            KEY='value#2'
            """,
            new EnvKVPair("KEY", "\"value#1\"", Optional.empty()),
            new EnvKVPair("KEY", "'value#2'", Optional.empty())
        );
    }

    @Test
    public void standalone_comment() throws EnvParseException {
        assertRoundTrip(
            "# This is a comment",
            new EnvComment("# This is a comment")
        );
    }

    @Test
    public void empty_line() throws EnvParseException {
        assertRoundTrip(
            """
            
            \s\s
            \t\s
            """,
            new EnvEmptyLine(""),
            new EnvEmptyLine("  "),
            new EnvEmptyLine("\t ")
        );
    }

    @Test
    public void mixed_nodes() throws EnvParseException {
        assertRoundTrip(
            """
            # comment
            
            KEY1=value1
            \s
            KEY2=value2   # inline
            KEY3="value3" # inline
            KEY4=
            """,
            new EnvComment("# comment"),
            new EnvEmptyLine(""),
            new EnvKVPair("KEY1", "value1", Optional.empty()),
            new EnvEmptyLine(" "),
            new EnvKVPair("KEY2", "value2", Optional.of(new EnvComment("   # inline"))),
            new EnvKVPair("KEY3", "\"value3\"", Optional.of(new EnvComment(" # inline"))),
            new EnvKVPair("KEY4", "", Optional.empty())
        );
    }

    @Test
    public void throws_on_empty_key() {
        val ex = assertThrows(EnvParseException.class, () ->
            new EnvParser().parseEnvFile(new Scanner("=value"))
        );
        assertThat(ex.getMessage()).contains("key cannot be empty");
    }

    @Test
    public void throws_on_unknown_syntax() {
        val ex = assertThrows(EnvParseException.class, () ->
            new EnvParser().parseEnvFile(new Scanner("whatever"))
        );
        assertThat(ex.getMessage()).contains("Unknown syntax");
    }
}
