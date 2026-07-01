package com.dtsx.astra.cli.snapshot.commands.dotenv;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;

public class DotEnvListKeysCmdSnapshotTest extends BaseCmdSnapshotTest {
    @TestForAllOutputs
    public void list_keys(OutputType outputType) {
        verifyRun("dotenv list-keys", outputType, o -> o);
    }
}
