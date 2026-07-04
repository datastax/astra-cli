package com.dtsx.astra.cli.operations.db.dataapi;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.dataapi.DbDataAPIExecOperation.DataAPIExecResult;
import com.dtsx.astra.cli.operations.db.dataapi.runners.DataAPIClientRunner;
import com.dtsx.astra.cli.operations.db.dataapi.runners.JavaScriptRunner;
import com.dtsx.astra.cli.operations.db.dataapi.runners.PythonRunner;
import com.dtsx.astra.cli.utils.DbUtils;
import com.dtsx.astra.cli.utils.FileUtils;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DbDataAPIExecOperation implements Operation<DataAPIExecResult> {
    private final CliContext ctx;
    private final DbGateway dbGateway;
    private final DbDataAPIExecRequest request;

    public enum Language {
        js, python
    }

    public record DbDataAPIExecRequest(
        Language language,
        DbRef dbRef,
        Optional<RegionName> region,
        Optional<String> keyspaceName,
        Optional<String> collectionName,
        Optional<String> tableName,
        Profile profile,
        List<String> packages,
        String extraCode,
        boolean isRepl,
        boolean captureOutput
    ) {}

    public record ExecContext(
        AstraToken token,
        AstraEnvironment env,
        Database db,
        String endpoint,
        String keyspace,
        Optional<String> collection,
        Optional<String> table
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
            val cacheDir = resolveCacheDir(runner);
            ctx.log().loading("Installing dependencies for the " + runner.languageName() + " client", (_) -> {
                runner.installDeps(cacheDir, request.packages());
                return null;
            });

            val initScript = runner.createInitScript(execCtx, request.extraCode());
            val pb = runner.executeCmd(cacheDir, initScript, request.isRepl());

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
        val keyspace = req.keyspaceName().orElseGet(() -> db.getInfo().getKeyspace());

        return new ExecContext(token, env, db, endpoint, keyspace, req.collectionName(), req.tableName());
    }

    private Path resolveCacheDir(DataAPIClientRunner runner) {
        val cacheDir = ctx.home().dirs.cache.use().resolve("dapi-exec").resolve(runner.languageName());
        FileUtils.createDirIfNotExists(cacheDir, "could not create cache directory for " + runner.languageName() + " dependencies");
        return cacheDir;
    }
}
