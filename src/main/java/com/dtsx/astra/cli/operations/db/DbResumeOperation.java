package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.Database;
import lombok.RequiredArgsConstructor;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;
import static com.dtsx.astra.cli.operations.db.DbResumeOperation.*;

@RequiredArgsConstructor
public class DbResumeOperation implements Operation<DbResumeResult> {
    private final DbGateway dbGateway;
    private final DbResumeRequest request;

    public record DbResumeResult(
        Database finalDatabase,
        DbGateway.ResumeDbResult resumeResult
    ) {}

    public record DbResumeRequest(
        DbRef dbRef,
        LongRunningOptions lrOptions
    ) {}

    @Override
    public DbResumeResult execute() {
        var resumeResult = dbGateway.resumeDb(request.dbRef, request.lrOptions.dontWait() ? 0 : request.lrOptions.timeout());
        var finalDatabase = dbGateway.findOneDb(request.dbRef);
        return new DbResumeResult(finalDatabase, resumeResult);
    }
}
