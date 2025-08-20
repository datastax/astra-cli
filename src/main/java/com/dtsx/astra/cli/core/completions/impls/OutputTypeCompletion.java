package com.dtsx.astra.cli.core.completions.impls;

import com.dtsx.astra.cli.core.completions.StaticCompletion;
import com.dtsx.astra.cli.core.output.formats.OutputType;

import java.util.Arrays;

public class OutputTypeCompletion extends StaticCompletion {
    public OutputTypeCompletion() {
        super(Arrays.stream(OutputType.values()).map(Enum::name).map(String::toLowerCase).toList());
    }
}
