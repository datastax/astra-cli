package com.dtsx.astra.cli.snapshot.commands.token;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;

public class TokenGetCmdSnapshotTest extends BaseCmdSnapshotTest {
    @TestForDifferentOutputs
    public void token_info(OutputType outputType) {
        verifyRun("token --token ${Token}", outputType, o -> o);
    }
}
