package com.dtsx.astra.cli.completions.impls;

import com.dtsx.astra.cli.completions.DynamicCompletion;
import com.dtsx.astra.cli.config.AstraConfig;

public class AvailableProfilesCompletion extends DynamicCompletion {
    public AvailableProfilesCompletion() {
        super("grep '^\\[.*\\]$' '" + AstraConfig.resolveDefaultAstraConfigFile() + "' | tr -d '[]' | tr '\\n' ' '");
    }
}
