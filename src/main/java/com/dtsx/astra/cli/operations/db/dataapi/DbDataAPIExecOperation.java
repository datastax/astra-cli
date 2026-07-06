package com.dtsx.astra.cli.operations.db.dataapi;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.BoxDrawer;
import com.dtsx.astra.cli.core.output.BoxDrawer.Alignment;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.dataapi.DbDataAPIExecOperation.DataAPIExecResult;
import com.dtsx.astra.cli.operations.db.dataapi.runners.JavaScriptRunner;
import com.dtsx.astra.cli.operations.db.dataapi.runners.PythonRunner;
import com.dtsx.astra.cli.utils.DbUtils;
import com.dtsx.astra.cli.utils.FileUtils;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.utils.CollectionUtils.windowed;

@RequiredArgsConstructor
public class DbDataAPIExecOperation implements Operation<DataAPIExecResult> {
    private final CliContext ctx;
    private final DbGateway dbGateway;
    private final DbDataAPIExecRequest request;

    @Getter
    @RequiredArgsConstructor
    public enum Language {
        js("JS"), python("Python");
        private final String displayName;
    }

    public record DbDataAPIExecRequest(
        Language language,
        DbRef dbRef,
        Optional<RegionName> region,
        KeyspaceRef ksRef,
        List<String> collectionNames,
        List<String> tableNames,
        Profile profile,
        List<String> packages,
        String extraCode,
        List<String> extraArgs, boolean isRepl,
        boolean captureOutput
    ) {}

    public record ExecContext(
        AstraToken token,
        AstraEnvironment env,
        Database db,
        String endpoint,
        String keyspace,
        List<String> collections,
        List<String> tables
    ) {}

    public sealed interface DataAPIExecResult {}
    public record InvalidDbStatus(DatabaseStatusType status) implements DataAPIExecResult {}
    public record OperationFailed(String error) implements DataAPIExecResult {}
    public record Executed(int exitCode) implements DataAPIExecResult {}
    public record ExecutedWithOutput(int exitCode, List<String> stdout, List<String> stderr) implements DataAPIExecResult {}

    @Override
    public DataAPIExecResult execute() {
        val execCtx = mkExecContext(request);

        if (execCtx.db.getStatus() != DatabaseStatusType.ACTIVE) {
            return new InvalidDbStatus(execCtx.db.getStatus());
        }

        val runner = switch (request.language()) {
            case js -> new JavaScriptRunner();
            case python -> new PythonRunner();
        };

        try {
            val cacheDir = resolveCacheDir(request.language());
            ctx.log().loading("Installing dependencies for the " + request.language().displayName + " client", (_) -> {
                runner.installDeps(cacheDir, request.packages());
                return null;
            });

            if (request.isRepl()) {
                val initVars = runner.createInitVars(execCtx);
                printHeader(initVars);
            }

            val initScript = runner.createInitScript(execCtx, request.extraCode());
            val pb = runner.executeCmd(cacheDir, initScript, request.extraArgs(), request.isRepl());

            if (request.captureOutput()) {
                val process = pb.start();

                val stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
                val stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                val output = stdOut.lines().toList();
                val error = stdErr.lines().toList();
                val exit = process.waitFor();
                return new ExecutedWithOutput(exit, output, error);
            } else {
                val exit = pb.inheritIO().start().waitFor();
                return new Executed(exit);
            }
        } catch (Exception e) {
            return new OperationFailed(e.getMessage());
        }
    }

    private ExecContext mkExecContext(DbDataAPIExecRequest req) {
        val token = request.profile().token();
        val env = request.profile().env();

        val db = dbGateway.findOne(req.dbRef());

        val region = DbUtils.resolveDatacenter(db, req.region()).getRegion();
        val endpoint = ApiLocator.getApiEndpoint(env, db.getId(), region);
        val keyspace = req.ksRef().name();

        return new ExecContext(token, env, db, endpoint, keyspace, req.collectionNames(), req.tableNames());
    }

    private Path resolveCacheDir(Language language) {
        val cacheDir = ctx.home().dirs.cache.use().resolve("dapi-exec").resolve(language.name());
        FileUtils.createDirIfNotExists(cacheDir, "could not create cache directory for " + language.name() + " dependencies");
        return cacheDir;
    }

    private void printHeader(List<String> initVars) {
        val lines = new ArrayList<String>() {{
            add(ctx.colors().format("Welcome to the @!%s Data API REPL!@".formatted(request.language().displayName)));
            add("");
            add("The following variables are available:");
            windowed(initVars, 2).forEach(pair -> add(ctx.colors().format(" @!*!@ " + String.join(", ", pair))));
        }};

        ctx.console().error(BoxDrawer.drawBox(3, ctx.colors().BLUE_300, lines, Alignment.LEFT));
    }
}
