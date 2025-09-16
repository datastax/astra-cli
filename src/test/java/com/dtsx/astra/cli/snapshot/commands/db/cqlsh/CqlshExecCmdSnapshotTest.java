package com.dtsx.astra.cli.snapshot.commands.db.cqlsh;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.TestConfig;

public class CqlshExecCmdSnapshotTest extends BaseCqlshExecCmd {
    private final SnapshotTestOptionsModifier dbFoundOpts = (o) -> o
        .forceProfile(TestConfig.profile())
        .use(baseDbFoundOpts);

    private final SnapshotTestOptionsModifier dbNotFoundOpts = (o) -> o
        .use(baseDbNotFoundOpts);

    private final SnapshotTestOptionsModifier disclaimerComments = (o) -> o
        .comment("Can't really test human output since we don't capture cqlsh output for human outputs")
        .comment("But at least we can verify the command runs and the exit code is correct")
        .comment("(human output returns cqlsh exit code; non-human output returns the cqlsh exit code via a field)");

    @TestForDifferentOutputs
    public void error_db_not_found(OutputType outputType) {
        verifyRun(escape("db", "cqlsh", "exec", "${DatabaseName}", "DESC KEYSPACES"), outputType, dbNotFoundOpts);
    }

    @TestForAllOutputs
    public void exec_string_success(OutputType outputType) {
        verifyRun(escape("db", "cqlsh", "exec", "${DatabaseName}", "DESC KEYSPACES"), outputType, dbFoundOpts.andThen(disclaimerComments));
    }

    @TestForAllOutputs
    public void exec_string_fail(OutputType outputType) {
        verifyRun(escape("db", "cqlsh", "exec", "${DatabaseName}", "DESC CARS"), outputType, dbFoundOpts.andThen(disclaimerComments));
    }

    @TestForAllOutputs
    public void exec_string_stdin_implied(OutputType outputType) {
        verifyRun(escape("db", "cqlsh", "exec", "${DatabaseName}"), outputType, o -> o.use(dbFoundOpts).use(disclaimerComments)
            .stdin("DESC KEYSPACES;"));
    }

    @TestForAllOutputs
    public void exec_string_stdin_explicit(OutputType outputType) {
        verifyRun(escape("db", "cqlsh", "exec", "${DatabaseName}", "-"), outputType, o -> o.use(dbFoundOpts).use(disclaimerComments)
            .stdin("DESC KEYSPACES;"));
    }

    @TestForDifferentOutputs
    public void exec_string_stdin_multiline(OutputType outputType) {
        verifyRun(escape("db", "cqlsh", "exec", "${DatabaseName}"), outputType, o -> o.use(dbFoundOpts).use(disclaimerComments)
            .stdin("DESC KEYSPACES;\\\nSHOW VERSION;\\\nHELP"));
    }
}
