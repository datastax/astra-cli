package com.dtsx.astra.cli.snapshot.commands.pcu;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Pcu;
import com.dtsx.astra.sdk.pcu.domain.PCUType;

import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PcuListTypesCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier mkOpts(Stream<PCUType> ret) {
        return (o) -> o
            .gateway(PcuGateway.class, (mock) -> {
                when(mock.findPcuTypes(any(), any())).thenReturn(ret);
            })
            .verify((mocks) -> {
                verify(mocks.pcuGateway()).findPcuTypes(Optional.empty(), Optional.empty());
            });
    }

    @TestForAllOutputs
    public void pcu_types_found(OutputType outputType) {
        verifyRun("pcu list-types", outputType, mkOpts(Pcu.Types.stream()));
    }

    @TestForDifferentOutputs
    public void no_pcu_types_found(OutputType outputType) {
        verifyRun("pcu list-types", outputType, mkOpts(Stream.of()));
    }
}
