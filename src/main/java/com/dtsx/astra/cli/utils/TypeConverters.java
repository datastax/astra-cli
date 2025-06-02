package com.dtsx.astra.cli.utils;

import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.exceptions.db.OptionValidationException;
import com.dtsx.astra.cli.output.output.OutputType;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.Getter;
import picocli.CommandLine.ITypeConverter;

import java.util.List;

public abstract class TypeConverters {
    public static List<TypeConverterWithClass> INSTANCES = List.of(
        new ToAstraEnvironment(),
        new ToOutputType(),
        new ToProfileName()
    );

    public abstract static class TypeConverterWithClass implements ITypeConverter<Object> {
        @Getter
        private final Class<Object> clazz;

        @SuppressWarnings("unchecked")
        public TypeConverterWithClass(Class<?> clazz) {
            this.clazz = (Class<Object>) clazz;
        }
    }

    private static class ToAstraEnvironment extends TypeConverterWithClass {
        public ToAstraEnvironment() {
            super(AstraEnvironment.class);
        }

        @Override
        public Object convert(String value) {
            return AstraEnvironment.valueOf(value.toUpperCase());
        }
    }

    private static class ToOutputType extends TypeConverterWithClass {
        public ToOutputType() {
            super(OutputType.class);
        }

        @Override
        public Object convert(String value) {
            return OutputType.valueOf(value.toUpperCase());
        }
    }

    private static class ToProfileName extends TypeConverterWithClass {
        public ToProfileName() {
            super(ProfileName.class);
        }

        @Override
        public Object convert(String value) {
            return ProfileName.parse(value).getRight((msg) -> {
                throw new OptionValidationException("profile name", msg);
            });
        }
    }
}
