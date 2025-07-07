package com.dtsx.astra.cli.core.completions.impls;

import com.dtsx.astra.cli.core.completions.DynamicCompletion;

import java.util.stream.Stream;

public class RoleNamesCompletion extends DynamicCompletion {
    static {
        register(new RoleNamesCompletion());
    }

    public RoleNamesCompletion() {
        super("""
          OUT=( $(cat "$(get_astra_dir)/completions-cache/role_names" 2>/dev/null | tr '\\n' ' ') )
       
          if [ -z "$names" ]; then
              OUT=(%s)
          fi
        """.formatted(defaultRoles()));
    }

    private static String defaultRoles() {
        return Stream.of(
            "R/W Svc Acct",
            "Admin User",
            "API Admin User",
            "Organization Administrator",
            "RO Svc Acct",
            "API RO Svc Acct",
            "API R/W User",
            "API Admin Svc Acct",
            "API R/W Svc Acct",
            "Billing Admin",
            "API RO User",
            "Database Administrator",
            "R/W User",
            "RO User",
            "UI View Only",
            "Admin Svc Acct"
        ).map(role -> "\"" + role + "\"")
         .reduce((a, b) -> a + " " + b)
         .orElse("");
    }
}
