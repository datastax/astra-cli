package com.dtsx.astra.cli.commands.dotenv;

import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.dotenv.DotEnvOperation.CreatedDotEnvContent;
import com.dtsx.astra.cli.operations.dotenv.DotEnvOperation.DotEnvResult;
import picocli.CommandLine.Command;

import java.util.function.Supplier;

import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@Command(
    name = "print",
    description = "Fill bound keys and print to stdout."
)
public class DotEnvPrintCmd extends AbstractDotEnvGenCmd {
    @Override
    protected boolean isPrint() {
        return true;
    }

    @Override
    protected OutputAll execute(Supplier<DotEnvResult> result) {
        return switch (result.get()) {
            case CreatedDotEnvContent(var content) -> OutputAll.response(
                content.render(ctx.colors()),
                sequencedMapOf("printed", true)
            );

            default -> throw new CongratsYouFoundABugException("Should not be able to get to default in DotEnvPrintCmd");
        };
    }
}
