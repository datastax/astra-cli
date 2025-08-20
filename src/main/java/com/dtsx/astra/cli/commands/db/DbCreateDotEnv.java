package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.db.DbCreateDotEnvOperation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.core.output.ExitCode.DOWNLOAD_ISSUE;
import static com.dtsx.astra.cli.operations.db.DbCreateDotEnvOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

@Command(
    name = "create-dotenv",
    description = "Create a env file to help you connect to your Astra DB instance."
)
@Example(
    comment = "Create a fully-populated .env file, updating the existing one if it exists",
    command = "astra db create-dotenv"
)
@Example(
    comment = "Create a .env file specifying the keys to include",
    command = "astra db create-dotenv --keys ASTRA_DB_APPLICATION_TOKEN,ASTRA_DB_API_ENDPOINT"
)
@Example(
    comment = "Create a .env file with a preset specifying the keys to include",
    command = "astra db create-dotenv --preset data_api_client"
)
@Example(
    comment = "Create a .env file specifying the keyspace and/or region to target",
    command = "astra db create-dotenv -k my_keyspace -r us-east1"
)
@Example(
    comment = "Print the .env file to stdout instead of saving it to a file",
    command = "astra db create-dotenv --print"
)
@Example(
    comment = "Specify the file to create/update",
    command = "astra db create-dotenv -f .env.local"
)
public class DbCreateDotEnv extends AbstractPromptForDbCmd<CreateDotEnvResult> {
    @Option(
        names = { "--print" },
        description = { "Output the .env file to stdout instead of saving it to the .env file.", DEFAULT_VALUE },
        paramLabel = "PRINT",
        defaultValue = "false"
    )
    private boolean $print;

    @Option(
        names = { "--overwrite" },
        description = { "Whether to existing duplicate keys in the .env file. If false, the command will attempt to prompt the user before overwriting any existing keys, or fail if it can't.", DEFAULT_VALUE },
        paramLabel = "PRINT",
        negatable = true
    )
    private Optional<Boolean> $overwrite;

    @Option(
        names = { "--file" },
        description = "The file to append the .env content to. If not specified, defaults to .env. If --print is true, the --file option will be used as the initial content; otherwise it will print a new .env file.",
        paramLabel = "FILE"
    )
    private Optional<File> $file;

    @Option(
        names = { "--keyspace", "-k" },
        description = "The keyspace to use. Uses the db's default keyspace if not specified.",
        paramLabel = "KEYSPACE"
    )
    private Optional<String> $keyspace;

    @Option(
        names = { "--region", "-r" },
        description = "The region to use. Uses the db's default region if not specified.",
        paramLabel = "REGION"
    )
    private Optional<RegionName> $region;

    @ArgGroup
    private @Nullable Keys $keysGroup;

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
            description = "The preset(s) to use for the .env file. If not specified, all keys (or the keys from `--keys`) will be used.",
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
    protected final OutputAll execute(Supplier<CreateDotEnvResult> result) {
        return switch (result.get()) {
            case CreatedDotEnvContent(var content) -> OutputAll.response(
                content.render(true),
                mkData("printed", false, null)
            );

            case CreatedDotEnvFile(var outputFile) -> OutputAll.response("""
              A new env file has been created at %s.
            
              Please double check the content of the file before using it, and manually add it to your .gitignore file.
            """.formatted(
                highlight(outputFile.getAbsolutePath())
            ), mkData("created", false, outputFile), List.of(
                new Hint("View the env file", "cat " + outputFile.getAbsolutePath())
            ));

            case UpdatedDotEnvFile(var outputFile, var overwritten) -> OutputAll.response("""
              The .env file has been updated at %s.

              If there were any duplicate keys, they %s.

              Please double check the content of the file before using it, and ensure it is added to your .gitignore file.
            """.formatted(
                highlight(outputFile.getAbsolutePath()),
                (overwritten) ? "were overwritten" : "were left in the env file, with the new keys added to the end"
            ), mkData("updated", overwritten, outputFile), List.of(
                new Hint("View the env file", "cat " + outputFile.getAbsolutePath())
            ));

            case NothingToUpdate(var outputFile) -> OutputAll.response("""
              No changes needed to be made to the .env file at %s.
            """.formatted(
                highlight(outputFile.getAbsolutePath())
            ), mkData("no_change", false, outputFile), List.of(
                new Hint("View the env file", "cat " + outputFile.getAbsolutePath())
            ));

            case FailedToDownloadSCB(var reason) -> throw new AstraCliException(DOWNLOAD_ISSUE, """
              @|bold, red Error: Failed to download the secure connect bundle.|@
            
              Cause:
              %s
            """.formatted(reason));
        };
    }

    private Map<String, Object> mkData(String status, boolean overwriteOccurred, @Nullable File file) {
        return Map.of(
            "status", status,
            "overwriteOccurred", overwriteOccurred,
            "file", Optional.ofNullable(file).map(File::getAbsolutePath)
        );
    }

    @Override
    protected DbCreateDotEnvOperation mkOperation() {
        val ksRef = $keyspace.map(ks -> KeyspaceRef.parse($dbRef, ks).fold(
            err -> { throw new OptionValidationException("keyspace", err); },
            Function.identity()
        ));

        return new DbCreateDotEnvOperation(dbGateway, orgGateway, downloadsGateway, new CreateDotEnvRequest(
            profile(),
            $dbRef,
            ksRef,
            $region,
            $file,
            $print,
            resolveKeys(),
            $overwrite,
            this::askIfShouldOverwrite
        ));
    }

    @Override
    protected String dbRefPrompt() {
        return "Select the database to create the .env file for";
    }

    private Set<EnvKey> resolveKeys() {
        if ($keysGroup != null && $keysGroup.keys != null) {
            return $keysGroup.keys;
        }

        if ($keysGroup != null && $keysGroup.preset != null) {
            return $keysGroup.preset.stream()
                .flatMap(p -> p.getKeys().stream())
                .collect(Collectors.toSet());
        }

        return Set.of(EnvKey.values());
    }

    private boolean askIfShouldOverwrite(Set<String> duplicates) {
        var duplicatesStr = duplicates.stream()
            .limit(4)
            .map(k -> "- " + highlight(k))
            .collect(Collectors.joining(NL));

        if (duplicates.size() > 4) {
            duplicatesStr += "\n- and %s more...".formatted(AstraColors.PURPLE_300.use(String.valueOf(duplicates.size() - 5)));
        }

        val msg = """
          Pre-existing keys found in the .env file:
          %s
  
          Note that these keys may not necessary have different values than what would be written.
  
          Do you want to overwrite them?
        """.formatted(duplicatesStr);

        return AstraConsole.confirm(msg)
            .defaultNo()
            .fallbackFlag("--overwrite")
            .fix(originalArgs(), "--overwrite")
            .dontClearAfterSelection();

//        if (response.equals(ConfirmResponse.NO_ANSWER)) {
//            throw new AstraCliException(EXECUTION_CANCELLED, """
//              @|bold, red Error: Operation was canceled because clashing keys were found in the .env file.|@
//
//              To avoid this error, you can:
//              - Interactively answer 'y' to overwrite the duplicate keys,
//              - Use the @!--overwrite!@ option to automatically overwrite the duplicate keys, or
//              - Use the @!--no-overwrite!@ option to leave the duplicate keys in the env file.
//            """);
//        }
    }
}
