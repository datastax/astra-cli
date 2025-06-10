package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.sdk.db.domain.Database;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DbResumeOperation {
    private final DbGateway dbGateway;

    public record DbResumeResult(
        Database finalDatabase,
        DbGateway.ResumeDbResult resumeResult
    ) {}

    public DbResumeResult execute(DbRef dbRef, boolean dontWait, Integer timeout) {
        var resumeResult = dbGateway.resumeDb(dbRef, dontWait ? 0 : timeout);
        var finalDatabase = dbGateway.findOneDb(dbRef);
        
        return new DbResumeResult(finalDatabase, resumeResult);
    }
}
