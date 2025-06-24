package com.dtsx.astra.cli.core;

import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.core.output.output.OutputType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.Getter;
import picocli.CommandLine.ITypeConverter;

import java.util.List;

public abstract class TypeConverters {
    public static List<TypeConverterWithClass> INSTANCES = List.of(
        new ToAstraEnvironment(),
        new ToOutputType(),
        new ToProfileName(),
        new ToDbRef(),
        new ToRegionRef()
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

    private static class ToDbRef extends TypeConverterWithClass {
        public ToDbRef() {
            super(DbRef.class);
        }

        @Override
        public Object convert(String value) {
            return DbRef.parse(value).getRight((msg) -> {
                throw new OptionValidationException("database name/id", msg);
            });
        }
    }

    private static class ToRegionRef extends TypeConverterWithClass {
        public ToRegionRef() {
            super(RegionName.class);
        }

        @Override
        public Object convert(String value) {
            return RegionName.parse(value).getRight((msg) -> {
                throw new OptionValidationException("region", msg);
            });
        }
    }
}
