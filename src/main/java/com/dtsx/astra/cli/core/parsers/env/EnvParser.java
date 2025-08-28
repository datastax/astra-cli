package com.dtsx.astra.cli.core.parsers.env;

import com.dtsx.astra.cli.core.parsers.env.ast.EnvComment;
import com.dtsx.astra.cli.core.parsers.env.ast.EnvEmptyLine;
import com.dtsx.astra.cli.core.parsers.env.ast.EnvKVPair;
import com.dtsx.astra.cli.core.parsers.env.ast.EnvNode;
import com.dtsx.astra.cli.utils.StringUtils;
import lombok.val;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
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
                handleKVPair(trimmedLine, lineNumber);
            } else {
                throw new EnvParseException("Unknown syntax", lineNumber, line);
            }
        }

        return new EnvFile(nodes);
    }

    private void handleKVPair(String line, int lineNumber) throws EnvParseException {
        int equalIndex = line.indexOf('=');

        val key = line.substring(0, equalIndex).trim();

        if (key.isEmpty()) {
            throw new EnvParseException("Invalid key-unwrap pair: key cannot be empty", lineNumber, line);
        }

        val value = StringUtils.removeQuotesIfAny(line.substring(equalIndex + 1).trim());
        nodes.add(new EnvKVPair(key, value));
    }
}
