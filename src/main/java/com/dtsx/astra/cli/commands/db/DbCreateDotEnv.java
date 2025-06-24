package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraConsole.ConfirmResponse;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.DbCreateDotEnvOperation;
import com.dtsx.astra.cli.operations.db.DbCreateDotEnvOperation.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.DbCreateDotEnvOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.NL;
import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;

@Command(
    name = "create-dotenv"
)
public class DbCreateDotEnv extends AbstractDbSpecificCmd<CreateDotEnvResult> {
    @Option(
        names = { "--print" },
        description = {"Output the .env file to stdout instead of saving it to the .env file.", DEFAULT_VALUE},
        paramLabel = "PRINT",
        defaultValue = "false"
    )
    private boolean print;

    @Option(
        names = { "--overwrite" },
        description = "Whether to existing duplicate keys in the .env file. If false, the command will attempt to prompt the user before overwriting any existing keys, or fail if it can't.",
        paramLabel = "PRINT",
        negatable = true
    )
    private Optional<Boolean> overwrite;

    @Option(
        names = { "--file" },
        description = "The file to append the .env content to. If not specified, defaults to .env. If --print is true, and this option is set, the file will be used as the initial content; otherwise it will print a new .env file.",
        paramLabel = "FILE"
    )
    private Optional<File> file;

    @Option(
        names = { "--keyspace", "-k" },
        description = "The keyspace to use",
        paramLabel = "KEYSPACE"
    )
    private Optional<String> keyspace;

    @Option(
        names = { "--region", "-r" },
        description = "The region to use",
        paramLabel = "REGION"
    )
    private Optional<RegionName> region;

    @ArgGroup
    private @Nullable Keys keysGroup;

    static class Keys {
        @Option(
            names = { "--keys" },
            description = "Comma-separated list of keys to include in the .env file. If not specified, all keys will be included.",
            split = ",",
            paramLabel = "KEYS"
        )
        private @Nullable Set<EnvKey> keys;

        @Option(
            names = { "--preset" },
            description = "The preset(s) to use for the .env file. If not specified, defaults to 'default'.",
            split = ",",
            paramLabel = "PRESETS"
        )
        private @Nullable Set<Preset> preset;

        @RequiredArgsConstructor
        enum Preset {
            data_api_client(Set.of(EnvKey.ASTRA_DB_APPLICATION_TOKEN, EnvKey.ASTRA_DB_API_ENDPOINT, EnvKey.ASTRA_DB_ENVIRONMENT));

            @Getter
            private final Set<EnvKey> keys;
        }
    }

    @Override
    protected DbCreateDotEnvOperation mkOperation() {
        val ksRef = keyspace.map(ks -> KeyspaceRef.parse(dbRef, ks).fold(
            err -> { throw new OptionValidationException("keyspace", err); },
            Function.identity()
        ));

        return new DbCreateDotEnvOperation(dbGateway, orgGateway, new CreateDotEnvRequest(
            profile(),
            dbRef,
            ksRef,
            region,
            file,
            print,
            resolveKeys(),
            overwrite,
            this::askIfShouldOverwrite
        ));
    }

    @Override
    protected final OutputAll execute(CreateDotEnvResult result) {
        return switch (result) {
            case CreatedDotEnvContent(var content) -> OutputAll.message(content.render(true));
            case CreatedDotEnvFile(var outputFile) -> OutputAll.message(trimIndent("""
              A new env file has been created at %s.
            
              Please double check the content of the file before using it, and manually add it to your .gitignore file.
            """.formatted(
                highlight(outputFile.getAbsolutePath())
            )));
            case UpdatedDotEnvFile(var outputFile, var overwritten) -> OutputAll.message(trimIndent("""
              The .env file has been updated at %s.

              If there were any duplicate keys, they %s.

              Please double check the content of the file before using it, and ensure it is added to your .gitignore file.
            """.formatted(
                highlight(outputFile.getAbsolutePath()),
                (overwritten) ? "were overwritten" : "were left in the env file, with the new keys added to the end"
            )));
            case NothingToUpdate(var outputFile) -> OutputAll.message(trimIndent("""
              No changes needed to be made to the .env file at %s.
            """.formatted(
                highlight(outputFile.getAbsolutePath())
            )));
        };
    }

    private Set<EnvKey> resolveKeys() {
        if (keysGroup != null && keysGroup.keys != null) {
            return keysGroup.keys;
        }

        if (keysGroup != null && keysGroup.preset != null) {
            return keysGroup.preset.stream()
                .flatMap(p -> p.getKeys().stream())
                .collect(Collectors.toSet());
        }

        return Set.of(EnvKey.values());
    }

    private Boolean askIfShouldOverwrite(Set<String> duplicates) {
        var duplicatesStr = duplicates.stream()
            .limit(4)
            .map(k -> "- " + highlight(k))
            .collect(Collectors.joining(NL));

        if (duplicates.size() > 4) {
            duplicatesStr += "\n- and %s more...".formatted(AstraColors.PURPLE_300.use(String.valueOf(duplicates.size() - 5)));
        }

        val msg = trimIndent("""
            Pre-existing keys found in the .env file:
            %s

            Note that these keys may not necessary have different values than what would be written.

            Do you want to overwrite them? [y/N]
            """.formatted(
            duplicatesStr
        ));

        return AstraConsole.confirm(msg).equals(ConfirmResponse.ANSWER_OK);
    }
}
