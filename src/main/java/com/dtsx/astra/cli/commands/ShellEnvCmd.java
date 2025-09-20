package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.operations.Operation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.ILLEGAL_OPERATION;

@Command(
    name = "shellenv",
    hidden = true
)
public class ShellEnvCmd extends AbstractCmd<Void> {
    @Override
    protected OutputHuman executeHuman(Supplier<Void> v) {
        val file = ProcessHandle.current().info().command().map(ctx::path);

        if (file.isEmpty()) {
            throw new AstraCliException(ILLEGAL_OPERATION, """
              @|bold,red Error: Can not run this command when not executing from a binary|@
            """);
        }

        return OutputHuman.response("""
          export PATH=%s:$PATH
          source <(%s compgen)
        """.formatted(file.get().getParent(), file.get()));
    }

    @Override
    protected Operation<Void> mkOperation() {
        return null;
    }
}
