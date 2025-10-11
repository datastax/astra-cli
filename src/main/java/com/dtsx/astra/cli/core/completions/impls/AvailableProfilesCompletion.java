package com.dtsx.astra.cli.core.completions.impls;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.core.completions.DynamicCompletion;

import java.util.Optional;

public class AvailableProfilesCompletion extends DynamicCompletion {
    static {
        register(new AvailableProfilesCompletion());
    }

    public AvailableProfilesCompletion() {
        super("""
          OUT=( $(grep '^\\[.*\\]$' '%s' | tr -d '[]') )
        """.formatted(
            Optional.ofNullable(AstraCli.unsafeGlobalCliContext()).map((ctx) -> ctx.get().properties().rcFileLocations(false).preferred()).orElse("~/.astrarc")
        ));
    }
}
