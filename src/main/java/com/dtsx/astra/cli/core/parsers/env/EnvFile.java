package com.dtsx.astra.cli.core.parsers.env;

import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.utils.StringUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

public record EnvFile(@Getter ArrayList<EnvNode> nodes) {
    @SneakyThrows
    public static EnvFile readEnvFile(Path path) throws EnvParseException, FileNotFoundException {
        if (!Files.exists(path)) {
            throw new FileNotFoundException(path.toString()); // todo catch if file is dir
        }

        try (val scanner = new Scanner(path)) {
            return new EnvParser().parseEnvFile(scanner);
        }
    }

    public void appendComment(String comment) {
        if (nodes.isEmpty() || !(nodes.getLast() instanceof EnvComment)) {
            nodes.add(new EnvComment(new ArrayList<>()));
        }
        ((EnvComment) nodes.getLast()).comments().add(comment);
    }

    public void appendVariable(String key, String value) {
        nodes.add(new EnvKVPair(List.of(), key, value));
    }

    public void deleteVariable(String key) {
        nodes.removeIf((n) -> (n instanceof EnvKVPair kv) && kv.key.equals(key));
    }

    public void filterNodes(java.util.function.Predicate<EnvNode> predicate) {
        nodes.removeIf(predicate.negate());
    }

    public List<EnvKVPair> getVariables() {
        return nodes.stream()
            .filter(EnvKVPair.class::isInstance)
            .map(EnvKVPair.class::cast)
            .toList();
    }

    public Optional<String> lookupKey(String key) {
        for (int i = nodes.size() - 1; i >= 0; i--) {
            if (nodes.get(i) instanceof EnvKVPair kv && key.equals(kv.key())) {
                return Optional.of(kv.value());
            }
        }
        return Optional.empty();
    }

    public String render(boolean colored) {
        val sj = new StringJoiner(NL);
        for (EnvNode node : nodes) {
            sj.add(node.render(colored));
        }
        return sj.toString();
    }

    @SneakyThrows
    public void writeToFile(Path file) {
        try (val writer = Files.newBufferedWriter(file)) {
            writer.write(render(false));
        }
    }

    public sealed interface EnvNode {
        String render(boolean colored);
    }

    public record EnvComment(List<String> comments) implements EnvNode {
        @Override
        public String render(boolean colored) {
            val sj = new StringJoiner(NL);

            for (String comment : comments) {
                sj.add((colored) ? AstraColors.NEUTRAL_400.use(comment) : comment);
            }

            return sj.toString();
        }
    }

    public record EnvNewline() implements EnvNode {
        @Override
        public String render(boolean colored) {
            return "";
        }
    }

    public record EnvKVPair(List<String> comments, String key, String value) implements EnvNode {
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

    private static class EnvParser {
        private final ArrayList<EnvNode> nodes = new ArrayList<>();
        private final List<String> currentComments = new ArrayList<>();
        private int lineNumber = 0;

        public EnvFile parseEnvFile(Scanner scanner) throws EnvParseException {
            while (scanner.hasNextLine()) {
                lineNumber++;
                val line = scanner.nextLine();
                val trimmedLine = line.trim();

                if (trimmedLine.isEmpty()) {
                    nodes.add(new EnvNewline());
                    continue;
                }

                if (trimmedLine.startsWith("#")) {
                    handleCommentedLine(line);
                } else if (trimmedLine.contains("=")) {
                    handleKVPair(trimmedLine, lineNumber);
                } else if (!trimmedLine.isBlank()) {
                    throw new EnvParseException("Unknown syntax", lineNumber, line);
                }
            }

            if (!currentComments.isEmpty()) {
                nodes.add(new EnvComment(new ArrayList<>(currentComments)));
                currentComments.clear();
            }

            return new EnvFile(nodes);
        }

        private void handleCommentedLine(String line) {
            currentComments.add(line);
        }

        private void handleKVPair(String line, int lineNumber) throws EnvParseException {
            int equalIndex = line.indexOf('=');

            val key = line.substring(0, equalIndex).trim();

            if (key.isEmpty()) {
                throw new EnvParseException("Invalid key-unwrap pair: key cannot be empty", lineNumber, line);
            }

            val value = StringUtils.removeQuotesIfAny(line.substring(equalIndex + 1).trim());

            nodes.add(new EnvKVPair(new ArrayList<>(currentComments), key, value));
            currentComments.clear();
        }
    }
}
