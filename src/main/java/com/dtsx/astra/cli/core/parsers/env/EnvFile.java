package com.dtsx.astra.cli.core.parsers.env;

import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.parsers.ParsedFile;
import com.dtsx.astra.cli.core.parsers.env.ast.EnvComment;
import com.dtsx.astra.cli.core.parsers.env.ast.EnvEmptyLine;
import com.dtsx.astra.cli.core.parsers.env.ast.EnvKVPair;
import com.dtsx.astra.cli.core.parsers.env.ast.EnvNode;
import lombok.*;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

@RequiredArgsConstructor
public class EnvFile extends ParsedFile {
    @Getter
    private final @NonNull List<EnvNode> nodes;

    public void appendComment(String comment) {
        nodes.add(new EnvComment(comment));
    }

    public void appendVariable(String key, String value) {
        nodes.add(new EnvKVPair(key, value, Optional.empty()));
    }

    public void appendNewLine() {
        nodes.add(new EnvEmptyLine(""));
    }

    public void deleteVariable(String key) {
        nodes.removeIf((n) -> (n instanceof EnvKVPair kv) && kv.key().equals(key));
    }

    public boolean updateVariable(String key, String value) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i) instanceof EnvKVPair kv && kv.key().equals(key)) {
                nodes.set(i, new EnvKVPair(key, value, kv.inlineComment()));
                return true;
            }
        }
        return false;
    }

    public void filterNodes(Predicate<EnvNode> predicate) {
        nodes.removeIf(predicate.negate());
    }

    public List<EnvKVPair> getVariables() {
        return nodes.stream()
            .filter(EnvKVPair.class::isInstance)
            .map(EnvKVPair.class::cast)
            .toList();
    }

    public Optional<String> lookupKey(String targetKey) {
        for (int i = nodes.size() - 1; i >= 0; i--) {
            if (nodes.get(i) instanceof EnvKVPair(var key, var value, var _) && key.equals(targetKey)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    @Override
    public String render(AstraColors colors) {
        val sj = new StringJoiner(NL);

        for (val node : nodes) {
            sj.add(node.render(colors));
        }

        return sj.toString();
    }

    public static EnvFile readFile(Path path) throws EnvParseException, FileNotFoundException {
        return ParsedFile.readFile(path, new EnvParser()::parseEnvFile);
    }
}
