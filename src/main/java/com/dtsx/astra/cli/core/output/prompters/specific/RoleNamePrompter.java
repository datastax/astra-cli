package com.dtsx.astra.cli.core.output.prompters.specific;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.sdk.org.domain.Role;
import lombok.val;

import java.util.List;

import static com.dtsx.astra.cli.core.output.ExitCode.ROLE_NOT_FOUND;

public class RoleNamePrompter {
    public static List<RoleRef> multiPrompt(CliContext ctx, RoleGateway gateway, String prompt, List<String> originalArgs) {
        val roles = gateway.findAll().toList();

        val options = NEList.parse(roles).orElseThrow(() -> new AstraCliException(ROLE_NOT_FOUND, "@|bold,red Error: no roles found to select from|@"));

        val uniqueNames = roles.stream().map(Role::getName).distinct().toList();

        return ctx.console().select(prompt)
            .multiOptions(options)
            .requireAnswer()
            .mapper(r -> {
                if (uniqueNames.contains(r.getName())) {
                    return r.getName();
                }
                return r.getName() + " @|faint (" + r.getId() + ")|@";
            })
            .fallbackFlag("-r")
            .fix(originalArgs, "-r <roles>")
            .clearAfterSelection()
            .stream()
            .map(r -> RoleRef.fromNameUnsafe(r.getName()))
            .toList();
    }
}
