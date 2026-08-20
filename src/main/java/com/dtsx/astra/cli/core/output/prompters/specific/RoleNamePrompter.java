package com.dtsx.astra.cli.core.output.prompters.specific;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.sdk.org.domain.DefaultRoles;
import lombok.val;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dtsx.astra.cli.core.output.ExitCode.ROLE_NOT_FOUND;

public class RoleNamePrompter {
    // whitelist so that we don't show tens of roles to the user, only the most common ones
    private static final Map<String, String> INCLUDE = new HashMap<>() {{
        compute("Enterprise Org Administrator", (k, _) -> k);
        compute("Enterprise Billing Administrator", (k, _) -> k);
        compute("Enterprise Administrator", (k, _) -> k);

        for (val role : DefaultRoles.values()) {
            put(role.getName(), role.getLabel());
        }
    }};

    public static List<RoleRef> multiPrompt(CliContext ctx, RoleGateway gateway, String prompt, List<String> originalArgs) {
        val roles = gateway.findAll()
            .filter(r -> INCLUDE.containsKey(r.getName()))
            .toList();

        val options = NEList.parse(roles).orElseThrow(() -> new AstraCliException(ROLE_NOT_FOUND, "@|bold,red Error: no common roles found to select from|@"));

        return ctx.console().select(prompt)
            .multiOptions(options)
            .requireAnswer()
            .mapper((r) -> INCLUDE.get(r.getName()))
            .fallbackFlag("-r")
            .fix(originalArgs, "-r <roles>")
            .clearAfterSelection()
            .stream()
            .map(r -> RoleRef.fromNameUnsafe(r.getName()))
            .toList();
    }
}
