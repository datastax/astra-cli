package com.dtsx.astra.cli.core.exceptions.internal.pcu;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.core.output.Hint;

import java.util.List;
import java.util.UUID;

import static com.dtsx.astra.cli.core.output.ExitCode.PCU_GROUP_NOT_FOUND;

public class PcuGroupNotFoundException extends AstraCliException {
    public PcuGroupNotFoundException(PcuRef pcuRef) {
        super(
            PCU_GROUP_NOT_FOUND,
            pcuRef.fold(PcuGroupNotFoundException::mkIdMsg, PcuGroupNotFoundException::mkNameMsg),
            pcuRef.fold(PcuGroupNotFoundException::mkIdHints, PcuGroupNotFoundException::mkNameHints)
        );
    }

    private static String mkIdMsg(UUID id) {
        return """
          @|bold,red Error: A PCU group with ID '%s' could not be found.|@

          Please ensure that:
            - You are using the correct token/organization.
            - You are using the correct PCU group uuid.
        """.formatted(id);
    }

    private static String mkNameMsg(String name) {
        return """
          @|bold,red Error: A PCU group named '%s' could not be found.|@

          Please ensure that:
            - You are using the correct token/organization.
            - You are using the correct PCU group name.
        """.formatted(name);
    }

    private static List<Hint> mkIdHints(UUID id) {
        return List.of(
            new Hint("List all PCU groups in your current org", "${cli.name} pcu list"),
            new Hint("Check your credentials", "${cli.name} config get <profile>")
        );
    }

    private static List<Hint> mkNameHints(String name) {
        return List.of(
            new Hint("List all PCU groups in your current org", "${cli.name} pcu list"),
            new Hint("Check your credentials", "${cli.name} config get <profile>"),
            new Hint("Create a new PCU group with the given name", "${cli.name} pcu create '" + name + "' <options>")
        );
    }
}
