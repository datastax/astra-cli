package com.dtsx.astra.cli.core.completions.impls;

import com.dtsx.astra.cli.core.completions.DynamicCompletion;
import com.dtsx.astra.cli.core.config.AstraConfig;

public class AvailableProfilesCompletion extends DynamicCompletion {
    static {
        register(new AvailableProfilesCompletion());
    }

    public AvailableProfilesCompletion() {
        super("""
          OUT=( $(grep '^\\[.*\\]$' '%s' | tr -d '[]') )
        """.formatted(
            AstraConfig.resolveDefaultAstraConfigFile()
        ));
    }
}
