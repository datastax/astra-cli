package com.dtsx.astra.cli.commands.db.cqlsh;

import com.dtsx.astra.cli.core.CliConstants.$Db;
import com.dtsx.astra.cli.core.CliConstants.$Keyspace;
import com.dtsx.astra.cli.core.CliConstants.$Regions;
import com.dtsx.astra.cli.core.completions.impls.DbNamesCompletion;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.prompters.specific.DbRefPrompter;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.CqlshExecResult;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshStartOperation;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshStartOperation.CqlshRequest;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshStartOperation.ExecSource;
import lombok.val;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

import static com.dtsx.astra.cli.core.output.ExitCode.IO_ISSUE;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

public abstract class CqlshStartImpl extends AbstractCqlshExecCmd {
    @Parameters(
        arity = "0..1",
        completionCandidates = DbNamesCompletion.class,
        description = "The name/ID of the Astra database to connect to",
        paramLabel = $Db.LABEL
    )
    public Optional<DbRef> $dbRef;

    @Option(
        names = { $Keyspace.LONG, $Keyspace.SHORT },
        description = "Authenticate to the given keyspace",
        paramLabel = $Keyspace.LABEL
    )
    public Optional<String> $keyspace;

    @Option(
        names = { "--request-timeout" },
        description = "Request timeout in seconds",
        paramLabel = "TIMEOUT",
        defaultValue = "20"
    )
    public int $requestTimeout;

    @Option(
        names = { $Regions.LONG, $Regions.SHORT },
        description = "The region to use. Uses the db's default region if not specified.",
        paramLabel = $Regions.LABEL
    )
    private Optional<RegionName> $region;

    protected abstract Optional<ExecSource> execSource();

    @Override
    protected Operation<CqlshExecResult> mkOperation(boolean captureOutput) {
        return new DbCqlshStartOperation(ctx, dbGateway, downloadsGateway, new CqlshRequest(
            $dbRef.orElseGet(this::promptForDb),
            $debug,
            $encoding,
            $keyspace,
            execSource(),
            $connectTimeout,
            $requestTimeout,
            $region,
            profile(),
            this::readStdin,
            captureOutput
        ));
    }

    private DbRef promptForDb() {
        return DbRefPrompter.prompt(ctx, dbGateway, "Select the database to work with:", (b) -> b.fallbackIndex(0).fix(originalArgs(), "<db>"));
    }

    private String readStdin() {
        try (val reader = new BufferedReader(new InputStreamReader(ctx.console().getIn()))) {
            val sb = new StringBuilder();
            var line = "";

            while ((line = reader.readLine()) != null) {
                if (line.trim().endsWith("\\")) {
                    sb.append(line, 0, line.lastIndexOf('\\') - 1); // TODO this should not be -1
                    sb.append(NL);
                } else {
                    sb.append(line);
                    break;
                }
            }

            return sb.toString();
        } catch (IOException e) {
            ctx.log().exception(e);

            throw new AstraCliException(IO_ISSUE, """
              @|bold,red Error: Attempted to read from standard input, but something went wrong:|@
            
              "%s"
            """.formatted(e.getMessage()));
        }
    }
}
