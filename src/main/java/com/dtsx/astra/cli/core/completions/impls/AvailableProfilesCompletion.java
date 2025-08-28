package com.dtsx.astra.cli.core.completions.impls;

import com.dtsx.astra.cli.core.CliProperties;
import com.dtsx.astra.cli.core.completions.DynamicCompletion;

public class AvailableProfilesCompletion extends DynamicCompletion {
    static {
        register(new AvailableProfilesCompletion());
    }

    public AvailableProfilesCompletion() {
        super("""
          OUT=( $(grep '^\\[.*\\]$' '%s' | tr -d '[]') )
        """.formatted(
            CliProperties.defaultRcFile(false)
        ));
    }
}
