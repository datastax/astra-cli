package com.dtsx.astra.cli.operations.dotenv;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.core.parsers.env.EnvFile;
import com.dtsx.astra.cli.core.parsers.env.ast.EnvComment;
import com.dtsx.astra.cli.core.parsers.env.ast.EnvKVPair;
import lombok.val;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class EnvKeysResolver {
    public static Map<String, EnvKey> resolveBindings(EnvFile source, Map<EnvKey, String> keys, Supplier<Set<EnvKey>> askForKeys) {
        val bindings = new LinkedHashMap<String, EnvKey>();

        resolveBindings(bindings, source);

        keys.forEach((key, mapping) -> {
            bindings.put((mapping.isBlank()) ? key.name() : mapping, key);
        });

        if (bindings.isEmpty()) {
            for (val key : askForKeys.get()) {
                bindings.put(key.name(), key);
            }
        }

        return bindings;
    }

    private static void resolveBindings(LinkedHashMap<String, EnvKey> bindings, EnvFile envFile) {
        val nodes = envFile.nodes();

        val contiguousComments = new ArrayDeque<EnvComment>();

        for (val node : nodes) {
            if (node instanceof EnvComment comment) {
                contiguousComments.add(comment);
            }

            if (node instanceof EnvKVPair kv) {
                EnvKey boundKey = null;

                kv.inlineComment().ifPresent(contiguousComments::add);

                for (val comment : contiguousComments.reversed()) {
                    val key = parseBinding(comment.comment());
                    if (key != null) {
                        boundKey = key;
                        break;
                    }
                }

                if (boundKey == null) {
                    try {
                        boundKey = EnvKey.valueOf(kv.key());
                    } catch (IllegalArgumentException _) {}
                }

                if (boundKey != null) {
                    bindings.put(kv.key(), boundKey);
                }

                contiguousComments.clear();
            }
        }
    }
    
    private static EnvKey parseBinding(String commentText) {
        if (commentText == null) return null;

        var trimmed = commentText.substring(commentText.indexOf("#") + 1).trim();

        if (trimmed.startsWith("astra:")) {
            trimmed = trimmed.substring("astra:".length()).trim();

            val keyEndIndex = trimmed.indexOf(' ');

            val keyName = (keyEndIndex >= 0)
                ? trimmed.substring(0, keyEndIndex)
                : trimmed;

            if (!keyName.equals(keyName.toUpperCase())) {
                return null; // just in case someone legitimately just has a random comment starting with 'astra:'
            }

            try {
                return EnvKey.valueOf(keyName);
            } catch (IllegalArgumentException e) {
                throw new AstraCliException(ExitCode.PARSE_ISSUE, """
                  @|bold,red Unknown Astra key in binding comment: '%s'|@
                
                  Run 'astra dotenv list-keys' to see valid keys.
                """.formatted(keyName));
            }
        }
        
        return null;
    }
}
