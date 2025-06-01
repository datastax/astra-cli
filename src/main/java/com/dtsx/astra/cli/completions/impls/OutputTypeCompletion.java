package com.dtsx.astra.cli.completions.impls;

import com.dtsx.astra.cli.completions.StaticCompletion;
import com.dtsx.astra.cli.output.output.OutputType;

import java.util.Arrays;

public class OutputTypeCompletion extends StaticCompletion {
    public OutputTypeCompletion() {
        super(Arrays.stream(OutputType.values()).map(Enum::name).map(String::toLowerCase).toList());
    }
}
