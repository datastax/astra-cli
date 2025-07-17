package com.dtsx.astra.cli.core.parsers.ini;

import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.utils.StringUtils;
import lombok.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.*;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

public record Ini(@Getter ArrayList<TopLevelIniNode> nodes) {
    public static Ini readIniFile(File file) throws IniParseException, FileNotFoundException {
        try (val scanner = new Scanner(file)) {
            return new IniParser().parseIniFile(scanner);
        }
    }

    public void addSection(String name, Map<String, String> pairs) {
        nodes.add(new IniSection(name, pairs.entrySet().stream().map(e -> new IniKVPair(List.of(), e.getKey(), e.getValue())).toList()));
    }

    public void addSection(String name, IniSection base) {
        nodes.add(new IniSection(name, new ArrayList<>(base.pairs())));
    }

    public void deleteSection(String name) {
        nodes.removeIf((n) -> (n instanceof IniSection s) && s.name.equals(name));
    }

    public List<IniSection> getSections() {
        return nodes.stream()
            .filter(IniSection.class::isInstance)
            .map(IniSection.class::cast)
            .toList();
    }

    public String render(boolean colored) {
        val sj = new StringJoiner(NL);
        for (TopLevelIniNode node : nodes) {
            sj.add(node.render(colored));
        }
        return sj.toString();
    }

    @SneakyThrows
    public void writeToFile(File file) {
        try (val writer = new FileWriter(file)) {
            writer.write(render(false));
        }
    }

    private sealed interface IniNode {
        String render(boolean colored);
    }

    private sealed interface TopLevelIniNode extends IniNode { }

    public record IniSection(String name, List<IniKVPair> pairs) implements TopLevelIniNode {
        @Override
        public String render(boolean colored) {
            val sj = new StringJoiner(NL);

            val header = "[" + name + "]";
            sj.add((colored) ? AstraColors.PURPLE_300.use(header) : header);

            for (IniKVPair pair : pairs) {
                sj.add(pair.render(colored));
            }

            return sj.toString();
        }

        public Optional<String> lookupKey(String key) {
            for (int i = pairs.size() - 1; i >= 0; i--) {
                if (key.equals(pairs.get(i).key())) {
                    return Optional.of(pairs.get(i).value());
                }
            }
            return Optional.empty();
        }
    }

    public record TopLevelComment(List<String> comments) implements TopLevelIniNode {
        @Override
        public String render(boolean colored) {
            val sj = new StringJoiner(NL);

            for (String comment : comments) {
                sj.add((colored) ? AstraColors.NEUTRAL_400.use(comment) : comment);
            }

            return sj.toString();
        }
    }

    public record IniKVPair(List<String> comments, String key, String value) implements IniNode {
        @Override
        @SuppressWarnings("DuplicatedCode")
        public String render(boolean colored) {
            val sb = new StringBuilder();

            for (String comment : comments) {
                sb.append((colored) ? AstraColors.NEUTRAL_400.use(comment) : comment);
                sb.append(NL);
            }

            if (colored) {
                sb.append(AstraColors.BLUE_300.use(key)).append(AstraColors.NEUTRAL_400.use("=")).append(value);
            } else {
                sb.append(key).append("=").append(value);
            }

            return sb.toString();
        }
    }

    private static class IniParser {
        private final ArrayList<TopLevelIniNode> nodes = new ArrayList<>();
        private final List<String> currentComments = new ArrayList<>();
        private int lineNumber = 0;

        public Ini parseIniFile(Scanner scanner) throws IniParseException {
            while (scanner.hasNextLine()) {
                lineNumber++;
                String line = scanner.nextLine();
                String trimmedLine = line.trim();

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

            return new Ini(nodes);
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
}
