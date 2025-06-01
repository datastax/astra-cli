package com.dtsx.astra.cli.config;

import com.dtsx.astra.cli.output.AstraColors;
import com.dtsx.astra.cli.utils.StringUtils;
import lombok.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Ini {
    @Getter
    private final ArrayList<TopLevelIniNode> nodes;

    public static Ini readIniFile(File file) {
        try (val scanner = new Scanner(file)) {
            return new IniParser().parseIniFile(scanner);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Given config file '" + file.getAbsoluteFile() + "' can not be found", e);
        }
    }

    public void addSection(String name, Map<String, String> pairs) {
        nodes.add(new IniSection(name, pairs.entrySet().stream().map(e -> new IniKVPair(List.of(), e.getKey(), e.getValue())).toList()));
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

    public String write(boolean colored) {
        val sj = new StringJoiner(NL);
        for (TopLevelIniNode node : nodes) {
            sj.add(node.write(colored));
        }
        return sj.toString();
    }

    @SneakyThrows
    public void writeToFile(File file) {
        try (val writer = new PrintWriter(new FileWriter(file))) {
            writer.print(write(false));
        }
    }

    private sealed interface IniNode {
        String write(boolean colored);
    }

    private sealed interface TopLevelIniNode extends IniNode {}

    public record IniSection(String name, List<IniKVPair> pairs) implements TopLevelIniNode {
        @Override
        public String write(boolean colored) {
            val sj = new StringJoiner(NL);

            val header = "[" + name + "]";
            sj.add((colored) ? AstraColors.PURPLE_300.use(header) : header);

            for (IniKVPair pair : pairs) {
                sj.add(pair.write(colored));
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
        public String write(boolean colored) {
            val sj = new StringJoiner(NL);

            for (String comment : comments) {
                sj.add((colored) ? AstraColors.NEUTRAL_300.use(comment) : comment);
            }

            return sj.toString();
        }
    }

    public record IniKVPair(List<String> comments, String key, String value) implements IniNode {
        @Override
        public String write(boolean colored) {
            val sb = new StringBuilder();

            for (String comment : comments) {
                sb.append((colored) ? AstraColors.NEUTRAL_300.use(comment) : comment);
                sb.append(NL);
            }

            if (colored) {
                sb.append(AstraColors.BLUE_300.use(key)).append(AstraColors.NEUTRAL_300.use("=")).append(value);
            } else {
                sb.append(key).append("=").append(value);
            }

            return sb.toString();
        }
    }

    static class IniParser {
        private final ArrayList<TopLevelIniNode> nodes = new ArrayList<>();
        private final List<String> currentComments = new ArrayList<>();
        private int lineNumber = 0;

        public Ini parseIniFile(Scanner scanner) {
            try {
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
                        handleSectionHeader(trimmedLine);
                    } else {
                        handleKVPair(trimmedLine);
                    }
                }

                if (!currentComments.isEmpty()) {
                    nodes.add(new TopLevelComment(new ArrayList<>(currentComments)));
                    currentComments.clear();
                }

                return new Ini(nodes);
            } catch (Exception e) {
                throw new IllegalArgumentException("Error parsing INI file at line " + lineNumber + ": " + e.getMessage(), e);
            }
        }

        private void handleCommentedLine(String line) {
            currentComments.add(line);
        }

        private void handleSectionHeader(String line) {
            if (!line.endsWith("]")) {
                throw new IllegalArgumentException("Invalid section header: missing ending closing bracket");
            }

            val sectionName = line.substring(1, line.length() - 1).trim();

            if (sectionName.isEmpty()) {
                throw new IllegalArgumentException("Invalid section header: section name cannot be empty");
            }

            if (!currentComments.isEmpty()) {
                nodes.add(new TopLevelComment(new ArrayList<>(currentComments)));
                currentComments.clear();
            }
            nodes.add(new IniSection(sectionName, new ArrayList<>()));
        }

        private void handleKVPair(String line) {
            int equalIndex = line.indexOf('=');

            if (equalIndex <= 0) {
                throw new IllegalArgumentException("Invalid key-value pair: missing or empty key");
            }

            val lastNode = nodes.getLast();

            if (lastNode instanceof IniSection lastSection) {
                val key = line.substring(0, equalIndex).trim();

                if (key.isEmpty()) {
                    throw new IllegalArgumentException("Invalid key-value pair: key cannot be empty");
                }

                val value = StringUtils.removeQuotesIfAny(line.substring(equalIndex + 1).trim());

                lastSection.pairs().add(new IniKVPair(new ArrayList<>(currentComments), key, value));
                currentComments.clear();
            } else {
                throw new IllegalArgumentException("Key-value pair found outside of section");
            }
        }
    }
}
