package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.output.output.OutputAll;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Optional;

import static com.dtsx.astra.cli.output.AstraColors.highlight;
import static com.dtsx.astra.cli.output.AstraColors.highlightStatus;

@Command(
    name = "resume"
)
public class DbResumeCmd extends AbstractLongRunningDbSpecificCmd {
    @Option(names = "--timeout", description = TIMEOUT_DESC, defaultValue = "600")
    protected void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public OutputAll execute() {
        return (dontWait) ? resumeNoWait() : resumeWaiting();
    }

    private OutputAll resumeNoWait() {
        val hadToBeResumed = dbService.resumeDb(dbRef, 0).hadToBeResumed();
        val currentStatus = dbService.getDbInfo(dbRef).getStatus();

        return OutputAll.message(
            (hadToBeResumed)
                ? "Database " + highlight(dbRef) + " was resumed, and " + ((currentStatus == DatabaseStatusType.ACTIVE) ? "is now active." : "currently has status " + highlightStatus(currentStatus) + ".")
                : "Database " + highlight(dbRef) + " was not in a state that required resuming, but will be active shortly, if it isn't already (current status: " + highlightStatus(currentStatus) + ")."
        );
    }

    private OutputAll resumeWaiting() {
        val resumedResult = dbService.resumeDb(dbRef, timeout);

        return OutputAll.message(
            (resumedResult.hadToBeResumed())
                ? "Database " + highlight(dbRef) + " was resumed, and is now active after waiting " + resumedResult.timeWaited().getSeconds() + " seconds." :
            (resumedResult.wasAwaited())
                ? "Database " + highlight(dbRef) + " was not in a state that required resuming, but is now active after waiting " + resumedResult.timeWaited().getSeconds() + " seconds."
                : "Database " + highlight(dbRef) + " is already active; no action was required."
        );
    }
}
