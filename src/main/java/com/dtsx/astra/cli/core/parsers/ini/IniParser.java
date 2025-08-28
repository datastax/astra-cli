package com.dtsx.astra.cli.core.parsers.ini;

import com.dtsx.astra.cli.core.parsers.ini.ast.IniKVPair;
import com.dtsx.astra.cli.core.parsers.ini.ast.IniSection;
import com.dtsx.astra.cli.core.parsers.ini.ast.TopLevelComment;
import com.dtsx.astra.cli.core.parsers.ini.ast.TopLevelIniNode;
import com.dtsx.astra.cli.utils.StringUtils;
import lombok.val;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@VisibleForTesting
public class IniParser {
    private final ArrayList<TopLevelIniNode> nodes = new ArrayList<>();
    private final List<String> currentComments = new ArrayList<>();
    private int lineNumber = 0;

    public IniFile parseIniFile(Scanner scanner) throws IniParseException {
        while (scanner.hasNextLine()) {
            lineNumber++;
            val line = scanner.nextLine();
            val trimmedLine = line.trim();

            if (trimmedLine.isEmpty()) {
                continue;
            }

            if (trimmedLine.startsWith("#")) {
                handleCommentedLine(trimmedLine);
            } else if (trimmedLine.startsWith("[")) {
                handleSectionHeader(trimmedLine, lineNumber);
            } else if (trimmedLine.contains("=")) {
                handleKVPair(trimmedLine, lineNumber);
            } else if (!trimmedLine.isBlank()) {
                throw new IniParseException("Unknown syntax", lineNumber, trimmedLine);
            }
        }

        if (!currentComments.isEmpty()) {
            nodes.add(new TopLevelComment(new ArrayList<>(currentComments)));
            currentComments.clear();
        }

        return new IniFile(nodes);
    }

    private void handleCommentedLine(String line) {
        currentComments.add(line);
    }

    private void handleSectionHeader(String line, int lineNumber) throws IniParseException {
        if (!line.endsWith("]")) {
            throw new IniParseException("Invalid section header: missing ending closing bracket", lineNumber, line);
        }

        val sectionName = line.substring(1, line.length() - 1).trim();

        if (sectionName.isBlank()) {
            throw new IniParseException("Invalid section header: section name cannot be blank or empty", lineNumber, line);
        }

        if (!currentComments.isEmpty()) {
            nodes.add(new TopLevelComment(new ArrayList<>(currentComments)));
            currentComments.clear();
        }
        nodes.add(new IniSection(sectionName, new ArrayList<>()));
    }

    private void handleKVPair(String line, int lineNumber) throws IniParseException {
        val lastNode = nodes.getLast();

        if (lastNode instanceof IniSection lastSection) {
            int equalIndex = line.indexOf('=');

            val key = line.substring(0, equalIndex).trim();

            if (key.isEmpty()) {
                throw new IniParseException("Invalid key-unwrap pair: key cannot be empty", lineNumber, line);
            }

            val value = StringUtils.removeQuotesIfAny(line.substring(equalIndex + 1).trim());

            lastSection.pairs().add(new IniKVPair(new ArrayList<>(currentComments), key, value));
            currentComments.clear();
        } else {
            throw new IniParseException("Key-unwrap pair found outside of section", lineNumber, line);
        }
    }
}
