package com.dtsx.astra.cli.completions.impls;

import com.dtsx.astra.cli.completions.StaticCompletion;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.Arrays;

public class AstraEnvCompletion extends StaticCompletion {
    public AstraEnvCompletion() {
        super(Arrays.stream(AstraEnvironment.values()).map(Enum::name).map(String::toLowerCase).toList());
    }
}
