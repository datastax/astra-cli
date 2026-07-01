package com.dtsx.astra.cli.core.parsers.env;

import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.parsers.env.ast.EnvComment;
import com.dtsx.astra.cli.core.parsers.env.ast.EnvEmptyLine;
import com.dtsx.astra.cli.core.parsers.env.ast.EnvKVPair;
import com.dtsx.astra.cli.core.parsers.env.ast.EnvNode;
import lombok.val;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

@VisibleForTesting
public class EnvParser {
    private final ArrayList<EnvNode> nodes = new ArrayList<>();
    private int lineNumber = 0;

    public EnvFile parseEnvFile(Scanner scanner) throws EnvParseException {
        while (scanner.hasNextLine()) {
            lineNumber++;
            val line = scanner.nextLine();
            val trimmedLine = line.trim();

            if (trimmedLine.isBlank()) {
                nodes.add(new EnvEmptyLine(line));
                continue;
            }

            if (trimmedLine.startsWith("#")) {
                nodes.add(new EnvComment(line));
            } else if (trimmedLine.contains("=")) {
                handleKVPair(trimmedLine, scanner);
            } else {
                throw new EnvParseException("Unknown syntax", lineNumber, line);
            }
        }

        return new EnvFile(nodes);
    }

    private void handleKVPair(String trimmedLine, Scanner scanner) throws EnvParseException {
        val keyEndIndex = findKeyEndIndex(trimmedLine);
        val valueStartIndex = findValueStartIndex(trimmedLine);
        val valueEndIndex = findValueEndIndex(trimmedLine, valueStartIndex, scanner);

        val key = trimmedLine.substring(0, keyEndIndex + 1).trim();
        val value = trimmedLine.substring(valueStartIndex, valueEndIndex).trim();

        val comment = (trimmedLine.lastIndexOf('#') >= valueEndIndex)
            ? trimmedLine.substring(valueEndIndex)
            : null;

        nodes.add(new EnvKVPair(key, value, Optional.ofNullable(comment).map(EnvComment::new)));
    }

    private int findKeyEndIndex(String line) throws EnvParseException {
        val equalIndex = line.indexOf('=');

        if (equalIndex < 0) {
            throw new EnvParseException("Invalid key-value pair: missing '='", lineNumber, line);
        }

        var keyEndIndex = equalIndex - 1;

        for (var i = keyEndIndex; i >= 0; i--) {
            if (!Character.isWhitespace(line.charAt(i))) {
                keyEndIndex = i;
                break;
            }
        }

        if (keyEndIndex <= 0) {
            throw new EnvParseException("Invalid key-value pair: key cannot be empty", lineNumber, line);
        }
        return keyEndIndex;
    }

    private int findValueStartIndex(String line) {
        val equalIndex = line.indexOf('=');

        if (equalIndex < 0) {
            throw new CongratsYouFoundABugException("Missing '=' should have been caught earlier");
        }

        var valueStartIndex = equalIndex + 1;

        for (var i = valueStartIndex; i < line.length(); i++) {
            if (!Character.isWhitespace(line.charAt(i))) {
                valueStartIndex = i;
                break;
            }
        }
        return valueStartIndex;
    }

    private int findValueEndIndex(String line, int startIndex, Scanner scanner) {
        var endIndex = line.length();

        if (startIndex >= line.length()) {
            return endIndex;
        }

        if (line.charAt(startIndex) == '"' || line.charAt(startIndex) == '\'') {
            var quoteChar = line.charAt(startIndex);

            outer: while (true) {
                for (int i = startIndex + 1; i < line.length(); i++) {
                    if (line.charAt(i) == quoteChar) {
                        endIndex = i + 1;
                        break outer;
                    }

                    if (line.charAt(i) == '\\') {
                        i++;
                    }
                }

                if (!scanner.hasNextLine()) {
                    break;
                }

                lineNumber++;
                line = scanner.nextLine();
                endIndex += line.length();
            }
        } else {
            for (int i = startIndex; i < line.length(); i++) {
                if (line.charAt(i) == '#') {
                    endIndex = startIndex;
                    for (int j = i - 1; j >= startIndex; j--) {
                        if (!Character.isWhitespace(line.charAt(j))) { // TODO clean this mess up
                            endIndex = j + 1;
                            break;
                        }
                    }
                    break;
                }

                if (line.charAt(i) == '\\') {
                    i++;
                }
            }
        }

        return endIndex;
    }
}
