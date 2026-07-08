package com.dtsx.astra.cli.snapshot.commands.db.dataapi;

import com.dtsx.astra.cli.core.exceptions.internal.db.DbNotFoundException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForHumanOutput;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContextOptions.TestCliContextOptionsBuilder;
import com.dtsx.astra.cli.utils.JsonUtils;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataAPIExecCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier opts = (o) -> o
        .useRealFs()
        .gateway(DbGateway.class, (mock) -> {
            when(mock.findOne(any())).thenReturn(Databases.One);
        })
        .verify((mocks) -> {
            verify(mocks.dbGateway()).findOne(Databases.NameRef);
        });


    private final SnapshotTestOptionsModifier invalidDbStatusOpts = (o) -> o
        .gateway(DbGateway.class, (mock) -> {
            Database badDb = JsonUtils.objectMapper().convertValue(Databases.One, Database.class);
            badDb.setStatus(DatabaseStatusType.HIBERNATED);
            when(mock.findOne(any())).thenReturn(badDb);
        })
        .verify((mocks) -> {
            verify(mocks.dbGateway()).findOne(Databases.NameRef);
        });

    private final SnapshotTestOptionsModifier notFoundDbOpts = (o) -> o
        .gateway(DbGateway.class, (mock) -> {
            when(mock.findOne(any())).thenThrow(new DbNotFoundException(DbRef.fromNameUnsafe("*nonexistent*")));
        })
        .verify((mocks) -> {
            verify(mocks.dbGateway()).findOne(DbRef.fromNameUnsafe("*nonexistent*"));
        });

    @TestForAllOutputs
    public void node_test_variables(OutputType outputType) {
        verifyRun("db data-api exec ${DatabaseName} -k ${Keyspace} -c ${CollectionName} -t ${TableName} -e console.log(!!client,\\ !!db,\\ !!test_coll,\\ !!test_table)", outputType, opts);
    }

    @TestForAllOutputs
    public void python_test_variables(OutputType outputType) {
        verifyRun("db data-api exec ${DatabaseName} -k ${Keyspace} -c ${CollectionName} -t ${TableName} --python -e print(client\\ is\\ not\\ None,\\ db\\ is\\ not\\ None,\\ test_coll\\ is\\ not\\ None,\\ test_table\\ is\\ not\\ None)", outputType, opts);
    }

    @TestForHumanOutput
    public void node_test_package(OutputType outputType) {
        verifyRun("db data-api exec ${DatabaseName} -k ${Keyspace} -a lodash -e console.log(require('lodash').camelCase('hello\\ world'))", outputType, opts);
    }

    @TestForHumanOutput
    public void script_syntax_error(OutputType outputType) {
        verifyRun("db data-api exec ${DatabaseName} -k ${Keyspace} -e console.log(!!client", outputType, opts);
    }

    @TestForHumanOutput
    public void error_multiple_languages(OutputType outputType) {
        verifyRun("db data-api exec ${DatabaseName} --node --python -e \"\"", outputType, TestCliContextOptionsBuilder::useRealFs);
    }

    @TestForAllOutputs
    public void error_db_not_found(OutputType outputType) {
        verifyRun("db data-api exec *nonexistent* -k ${Keyspace} -e \"\"", outputType, notFoundDbOpts);
    }

    @TestForAllOutputs
    public void error_invalid_db_status(OutputType outputType) {
        verifyRun("db data-api exec ${DatabaseName} -k ${Keyspace} -e \"\"", outputType, invalidDbStatusOpts);
    }
}
