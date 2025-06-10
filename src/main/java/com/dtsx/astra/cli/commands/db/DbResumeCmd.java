package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.operations.db.DbResumeOperation;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "resume"
)
public final class DbResumeCmd extends AbstractLongRunningDbSpecificCmd {
    @Option(names = "--timeout", description = TIMEOUT_DESC, defaultValue = "600")
    protected void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    private DbResumeOperation dbResumeOperation;

    @Override
    protected void prelude() {
        super.prelude();
        this.dbResumeOperation = new DbResumeOperation(dbGateway);
    }

    @Override
    public OutputAll execute() {
        val result = dbResumeOperation.execute(dbRef, dontWait, timeout);

        if (dontWait) {
            val hadToBeResumed = result.resumeResult().hadToBeResumed();
            val currentStatus = result.finalDatabase().getStatus();

            return OutputAll.message(
                (hadToBeResumed)
                    ? "Database " + highlight(dbRef) + " was resumed, and " + ((currentStatus == DatabaseStatusType.ACTIVE) ? "is now active." : "currently has currStatus " + AstraColors.highlight(currentStatus) + ".")
                    : "Database " + highlight(dbRef) + " was not in a state that required resuming, but will be active shortly, if it isn't already (current currStatus: " + AstraColors.highlight(currentStatus) + ")."
            );
        } else {
            val resumedResult = result.resumeResult();

            return OutputAll.message(
                (resumedResult.hadToBeResumed())
                    ? "Database " + highlight(dbRef) + " was resumed, and is now active after waiting " + resumedResult.timeWaited().getSeconds() + " seconds." :
                (resumedResult.wasAwaited())
                    ? "Database " + highlight(dbRef) + " was not in a state that required resuming, but is now active after waiting " + resumedResult.timeWaited().getSeconds() + " seconds."
                    : "Database " + highlight(dbRef) + " is already active; no action was required."
            );
        }
    }
}
