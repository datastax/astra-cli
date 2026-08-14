package com.dtsx.astra.cli.core.completions.impls;

import com.dtsx.astra.cli.core.completions.StaticCompletion;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.List;

public class AstraEnvCompletion extends StaticCompletion {
    public AstraEnvCompletion() {
        super(List.of(AstraEnvironment.allValuesLower()));
    }
}
