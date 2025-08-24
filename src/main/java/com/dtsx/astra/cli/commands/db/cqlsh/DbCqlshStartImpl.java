package com.dtsx.astra.cli.commands.db.cqlsh;

import com.dtsx.astra.cli.core.completions.impls.DbNamesCompletion;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
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

import static com.dtsx.astra.cli.utils.StringUtils.NL;

public abstract class DbCqlshStartImpl extends AbstractCqlshExecCmd {
    @Parameters(
        index = "0",
        completionCandidates = DbNamesCompletion.class,
        paramLabel = "DB",
        description = "The name/ID of the Astra database to connect to"
    )
    public DbRef $dbRef;

    @Option(
        names = { "-k", "--keyspace" },
        description = "Authenticate to the given keyspace",
        paramLabel = "KEYSPACE"
    )
    public Optional<String> $keyspace;

    @Option(
        names = { "--request-timeout" },
        description = { "Request timeout in seconds", DEFAULT_VALUE },
        paramLabel = "TIMEOUT",
        defaultValue = "20"
    )
    public int $requestTimeout;

    protected abstract Optional<ExecSource> execSource();

    @Override
    protected Operation<CqlshExecResult> mkOperation(boolean captureOutput) {
        return new DbCqlshStartOperation(dbGateway, downloadsGateway, new CqlshRequest(
            $dbRef,
            $debug,
            $encoding,
            $keyspace,
            execSource(),
            $connectTimeout,
            $requestTimeout,
            profile(),
            this::readStdin,
            captureOutput
        ));
    }

    private String readStdin() {
        try (val reader = new BufferedReader(new InputStreamReader(AstraConsole.getIn()))) {
            val sb = new StringBuilder();
            var line = "";

            while ((line = reader.readLine()) != null) {
                if (line.trim().endsWith("\\")) {
                    sb.append(line, 0, line.lastIndexOf('\\') - 1);
                    sb.append(NL);
                } else {
                    sb.append(line);
                    break;
                }
            }

            return sb.toString();
        } catch (IOException e) {
            AstraLogger.exception(e);

            throw new AstraCliException("""
              @|bold,red Error: Attempted to read from standard input, but something went wrong:|@
            
              "%s"
            """.formatted(e.getMessage()));
        }
    }
}
