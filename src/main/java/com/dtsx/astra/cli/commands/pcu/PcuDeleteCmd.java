package com.dtsx.astra.cli.commands.pcu;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.pcu.PcuDeleteOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.EXECUTION_CANCELLED;
import static com.dtsx.astra.cli.core.output.ExitCode.PCU_GROUP_NOT_FOUND;
import static com.dtsx.astra.cli.operations.pcu.PcuDeleteOperation.*;
import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;

@Command(
    name = "delete",
    description = "Delete an existing Pcu group"
)
@Example(
    comment = "Delete an existing Pcu group",
    command = "${cli.name} pcu delete my_pcu"
)
@Example(
    comment = "Delete an existing Pcu group without confirmation (required for non-interactive shells)",
    command = "${cli.name} pcu delete my_pcu --yes"
)
@Example(
    comment = "Delete an existing Pcu group without failing if it does not exist",
    command = "${cli.name} pcu delete my_pcu --if-exists"
)
public class PcuDeleteCmd extends AbstractPromptForPcuCmd<PcuDeleteResult> {
    @Option(
        names = { "--if-exists" },
        description = "Do not fail if PCU group does not exist",
        defaultValue = "false"
    )
    public boolean $ifExists;

    @Option(
        names = { "--yes" },
        description = "Force deletion of PCU group without prompting",
        defaultValue = "false"
    )
    public boolean $forceDelete;

    @Override
    protected final OutputAll execute(Supplier<PcuDeleteResult> result) {
        return switch (result.get()) {
            case PcuNotFound() -> handlePcuNotFound($pcuRef);
            case PcuDeleted() -> handlePcuDeleted($pcuRef);
            case PcuIllegallyNotFound() -> throwPcuNotFound($pcuRef);
        };
    }

    private OutputAll handlePcuNotFound(PcuRef pcuRef) {
        val message = "PCU group %s does not exist; nothing to delete.".formatted(
            ctx.highlight(pcuRef)
        );

        return OutputAll.response(message, mkData(false), List.of(
            new Hint("See your existing PCU groups:", "${cli.name} pcu list")
        ));
    }

    private OutputAll handlePcuDeleted(PcuRef pcuRef) {
        val message = "PCU group %s has been deleted.".formatted(
            ctx.highlight(pcuRef)
        );

        return OutputAll.response(message, mkData(true));
    }

    private <T> T throwPcuNotFound(PcuRef pcuRef) {
        throw new AstraCliException(PCU_GROUP_NOT_FOUND, """
          @|bold,red Error: PCU group '%s' could not be found.|@
        
          To ignore this error, you can use the @'!--if-exists!@ option to avoid failing if the pcu does not exist.
        """.formatted(
            pcuRef
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-exists"),
            new Hint("See your existing PCU groups:", "${cli.name} pcu list")
        ));
    }

    @Override
    protected PcuDeleteOperation mkOperation() {
        return new PcuDeleteOperation(pcuGateway, new PcuDeleteRequest(
            $pcuRef,
            $ifExists,
            $forceDelete,
            this::assertShouldDelete
        ));
    }

    private void assertShouldDelete(String pcuName, UUID id) {
        val prompt = """
          You are about to permanently delete PCU group @!%s!@ @|faint (%s)|@.
        
          To confirm, type the name below or press @!Ctrl+C!@ to cancel.
        """.formatted(pcuName, id);

        val shouldDelete = ctx.console().prompt(prompt)
            .mapper(Function.identity())
            .requireAnswer()
            .fallbackFlag("--yes")
            .fix(originalArgs(), "--yes")
            .clearAfterSelection()
            .equals(pcuName);

        if (!shouldDelete) {
            throw new AstraCliException(EXECUTION_CANCELLED, """
              @|bold,red Error: User input did not match PCU group name.|@
            
              Pcu @!%s!@ was not deleted.
            """.formatted(pcuName), List.of(
                new Hint("Skip confirmation prompt:", originalArgs(), "--yes")
            ));
        }
    }

    private LinkedHashMap<String, Object> mkData(boolean wasDeleted) {
        return sequencedMapOf(
            "wasDeleted", wasDeleted
        );
    }

    @Override
    protected String pcuRefPrompt() {
        return "Select the PCU group to delete";
    }
}
