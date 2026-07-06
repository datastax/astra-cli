package com.dtsx.astra.cli.commands.pcu;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.PcuStatus;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.PcuStatusOperation;
import com.dtsx.astra.cli.operations.pcu.PcuStatusOperation.PcuStatusRequest;
import picocli.CommandLine.Command;

import java.util.function.Supplier;

@Command(
    name = "status",
    description = "Get the current status of a PCU group."
)
@Example(
    comment = "Get the status of a PCU group",
    command = "${cli.name} db status my_db"
)
public class PcuStatusCmd extends AbstractPromptForPcuCmd<PcuStatus> {
    @Override
    protected Operation<PcuStatus> mkOperation() {
        return new PcuStatusOperation(ctx, pcuGateway, new PcuStatusRequest($pcuRef));
    }

    @Override
    protected final OutputHuman executeHuman(Supplier<PcuStatus> result) {
        return OutputHuman.response("PCU group %s is %s".formatted(ctx.highlight($pcuRef), ctx.highlight(result.get())));
    }

    @Override
    protected final OutputAll execute(Supplier<PcuStatus> result) {
        return OutputAll.serializeValue(result.get());
    }

    @Override
    protected String pcuRefPrompt() {
        return "Select the PCU group to get the status for";
    }
}
