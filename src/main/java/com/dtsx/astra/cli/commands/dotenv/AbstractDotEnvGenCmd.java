package com.dtsx.astra.cli.commands.dotenv;

import com.dtsx.astra.cli.commands.db.AbstractDbCmd;
import com.dtsx.astra.cli.core.CliConstants.$Db;
import com.dtsx.astra.cli.core.CliConstants.$Keyspace;
import com.dtsx.astra.cli.core.CliConstants.$Regions;
import com.dtsx.astra.cli.core.completions.impls.DbNamesCompletion;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.core.output.prompters.specific.DbRefPrompter;
import com.dtsx.astra.cli.operations.dotenv.DotEnvOperation;
import com.dtsx.astra.cli.operations.dotenv.DotEnvOperation.DotEnvRequest;
import com.dtsx.astra.cli.operations.dotenv.DotEnvOperation.DotEnvResult;
import com.dtsx.astra.cli.operations.dotenv.EnvKey;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractDotEnvGenCmd extends AbstractDbCmd<DotEnvResult> {
    @Option(
        names = { "--file", "-f" },
        description = "The target .env file to update.",
        paramLabel = "FILE"
    )
    protected Optional<Path> $file;

    @Option(
        names = { "--keys", "-k" },
        description = "Comma-separated EnvKey names to filter the resolved binding set.",
        mapFallbackValue = "",
        split = ","
    )
    protected Map<EnvKey, String> $keys = Map.of();

    @Option(
        names = { "--overwrite" },
        description = "Always overwrite non-empty values.",
        negatable = true
    )
    protected Optional<Boolean> $overwrite;

    @Option(
        names = { "--db" },
        description = "Database to bind to.",
        completionCandidates = DbNamesCompletion.class,
        paramLabel = $Db.LABEL
    )
    protected @Nullable DbRef $dbRef;

    @Option(
        names = { $Keyspace.LONG },
        description = "The keyspace to use.",
        paramLabel = $Keyspace.LABEL
    )
    protected Optional<String> $keyspace;

    @Option(
        names = { $Regions.LONG },
        description = "The region to use.",
        paramLabel = $Regions.LABEL
    )
    protected Optional<RegionName> $region;

    protected abstract boolean isPrint();

    @Override
    protected DotEnvOperation mkOperation() {
        val ksRef = $keyspace.map(ks -> KeyspaceRef.parse($dbRef, ks).fold(
            err -> {
                throw new AstraCliException(ExitCode.VALIDATION_ISSUE, "keyspace " + err);
            },
            Function.identity()
        ));

        val downloadsGateway = ctx.gateways().mkDownloadsGateway();
        val orgGateway = ctx.gateways().mkOrgGateway(profile().token(), profile().env());

        return new DotEnvOperation(ctx, dbGateway, orgGateway, downloadsGateway, new DotEnvRequest(
            profile(),
            $dbRef,
            ksRef,
            $region,
            $file,
            isPrint(),
            $keys,
            $overwrite,
            this::askForKeys,
            this::askForDbRef,
            this::askIfShouldOverwrite
        ));
    }

    private DbRef askForDbRef() {
        return DbRefPrompter.prompt(ctx, dbGateway, "Select database to bind to", f -> f.fallbackFlag("--db").fix(originalArgs(), "--db"));
    }

    private Set<EnvKey> askForKeys() {
        val allKeys = Arrays.asList(EnvKey.values());

        val selected = new HashSet<>(
            ctx.console().select("Select the keys you want to generate in your .env file")
                .multiOptions(allKeys.toArray(new EnvKey[0]))
                .requireAnswer()
                .mapper(EnvKey::name)
                .fallbackFlag("--keys")
                .fix(originalArgs(), "--keys ASTRA_ORG_TOKEN")
                .clearAfterSelection()
        );

        if (selected.isEmpty()) {
            throw new AstraCliException(ExitCode.VALIDATION_ISSUE, "No keys selected");
        }

        return selected;
    }

    private boolean askIfShouldOverwrite(Set<String> duplicates) {
        var duplicatesStr = duplicates.stream()
            .limit(4)
            .map(k -> "- " + ctx.highlight(k))
            .collect(Collectors.joining("\n"));

        if (duplicates.size() > 4) {
            duplicatesStr += "\n- and %s more...".formatted(ctx.colors().PURPLE_300.use(String.valueOf(duplicates.size() - 5)));
        }

        val msg = """
        Pre-existing keys with values found in the .env file:
        %s
        
        Do you want to overwrite them?
        """.formatted(duplicatesStr);

        return ctx.console().confirm(msg)
            .defaultNo()
            .fallbackFlag("--overwrite")
            .fix(originalArgs(), "--overwrite")
            .dontClearAfterSelection();
    }
}
