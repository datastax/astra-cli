package com.dtsx.astra.cli.utils;

import com.dtsx.astra.cli.output.output.OutputType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import picocli.CommandLine;

public class TypeConverters {
    public static class ToAstraEnvironment implements CommandLine.ITypeConverter<AstraEnvironment> {
        @Override
        public AstraEnvironment convert(String value) {
            return AstraEnvironment.valueOf(value.toUpperCase());
        }
    }

    public static class ToOutputType implements CommandLine.ITypeConverter<OutputType> {
        @Override
        public OutputType convert(String value) {
            return OutputType.valueOf(value.toUpperCase());
        }
    }
}
