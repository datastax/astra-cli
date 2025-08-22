package com.dtsx.astra.cli.core.completions.impls;

import com.dtsx.astra.cli.core.completions.StaticCompletion;
import com.dtsx.astra.cli.core.config.AstraConfig;

public class ProfileKeysCompletion extends StaticCompletion {
    public ProfileKeysCompletion() {
        super(AstraConfig.TOKEN_KEY, AstraConfig.ENV_KEY);
    }
}
