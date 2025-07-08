package com.dtsx.astra.cli.core;

import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.*;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

import java.util.List;

public abstract class TypeConverters {
    public static List<TypeConverter> INSTANCES = List.of(
        new TypeConverter(ProfileName.class, ProfileName::parse),
        new TypeConverter(DbRef.class, DbRef::parse),
        new TypeConverter(RegionName.class, RegionName::parse),
        new TypeConverter(UserRef.class, UserRef::parse),
        new TypeConverter(AstraToken.class, AstraToken::parse),
        new TypeConverter(TenantName.class, TenantName::parse),
        new TypeConverter(RoleRef.class, RoleRef::parse)
    );

    private interface Parseable {
        Either<String, ?> parse(String value);
    }

    @RequiredArgsConstructor
    public static class TypeConverter implements ITypeConverter<Object> {
        private final Class<?> clazz;
        private final Parseable parseable;

        @SuppressWarnings("unchecked")
        public Class<Object> clazz() {
            return (Class<Object>) clazz;
        }

        @Override
        public Object convert(String value) {
            return parseable.parse(value).getRight((msg) -> {
                throw new TypeConversionException(msg);
            });
        }
    }
}
