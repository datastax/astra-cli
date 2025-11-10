package com.dtsx.astra.cli.core.completions.impls;

import com.dtsx.astra.cli.core.completions.DynamicCompletion;

public class AvailableProfilesCompletion extends DynamicCompletion {
    static {
        register(new AvailableProfilesCompletion());
    }

    public AvailableProfilesCompletion() {
        super("""
          RC_FILE=$(get_astra_rc); [ -f "$RC_FILE" ]&&OUT=( $(grep '^\\[.*\\]$' "$RC_FILE" | tr -d '[]') )||OUT=()
        """);
    }
}
