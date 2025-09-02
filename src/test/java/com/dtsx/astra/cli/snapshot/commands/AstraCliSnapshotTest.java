package com.dtsx.astra.cli.snapshot.commands;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;

public class AstraCliSnapshotTest extends BaseCmdSnapshotTest {
    @TestForAllOutputs
    public void help_output(OutputType outputType) {
        verifyRun("", outputType, o -> o);
    }
}
