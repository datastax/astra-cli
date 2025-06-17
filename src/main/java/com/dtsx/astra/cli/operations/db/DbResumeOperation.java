package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.sdk.db.domain.Database;
import lombok.RequiredArgsConstructor;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;

@RequiredArgsConstructor
public class DbResumeOperation {
    private final DbGateway dbGateway;

    public record DbResumeResult(
        Database finalDatabase,
        DbGateway.ResumeDbResult resumeResult
    ) {}

    public DbResumeResult execute(DbRef dbRef, LongRunningOptions lrOptions) {
        var resumeResult = dbGateway.resumeDb(dbRef, lrOptions.dontWait() ? 0 : lrOptions.timeout());
        var finalDatabase = dbGateway.findOneDb(dbRef);
        return new DbResumeResult(finalDatabase, resumeResult);
    }
}
