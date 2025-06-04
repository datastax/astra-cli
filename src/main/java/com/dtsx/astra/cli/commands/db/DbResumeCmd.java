package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.output.AstraColors;
import com.dtsx.astra.cli.output.output.OutputAll;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Optional;

@Command(
    name = "resume"
)
public class DbResumeCmd extends AbstractLongRunningDbSpecificCmd {
    @Option(names = "--timeout", description = TIMEOUT_DESC, defaultValue = "600")
    protected void setTimeout(int timeout) {
        this.timeout = Optional.of(timeout);
    }

    @Override
    public OutputAll execute() {
        val hadToBeResumed = dbService.resumeDb(dbName);
        val coloredDbName = AstraColors.BLUE_300.use(dbName);

        if (async) {
            return OutputAll.message((hadToBeResumed)
                ? "Database " + coloredDbName + " was resumed, but may not be active yet."
                : "Database " + coloredDbName + " was not in a state that required resuming, but will be active shortly, if it isn't already."
            );
        }

        val hadToBeAwaited = dbService.waitUntilDbActive(dbName, timeout.orElseThrow());

        val message =
            (hadToBeResumed)
                ? "has been resumed and is now active." :
            (hadToBeAwaited)
                ? "is now active."
                : "is already active; no action was required.";

        return OutputAll.message("Database " + coloredDbName + " " + message);
    }
}
