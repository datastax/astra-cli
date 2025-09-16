package com.dtsx.astra.cli.core;

import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.datatypes.Ref;
import com.dtsx.astra.cli.core.models.*;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class TypeConverters {
    public static List<? extends ContextualTypeConverter> mkInstances(Ref<CliContext> ctxRef) {
        return List.of(
            new ContextlessTypeConverter(AstraToken.class, AstraToken::parse),
            new ContextlessTypeConverter(DbRef.class, DbRef::parse),
            new ContextlessTypeConverter(RegionName.class, RegionName::parse),
            new ContextlessTypeConverter(RoleRef.class, RoleRef::parse),
            new ContextlessTypeConverter(TenantName.class, TenantName::parse),
            new ContextlessTypeConverter(UserRef.class, UserRef::parse),
            new ContextlessTypeConverter(ProfileName.class, ProfileName::parse),
            new ContextualTypeConverter(Path.class, Misc::parsePath, ctxRef)
        );
    }

    @RequiredArgsConstructor
    public static class ContextualTypeConverter implements ITypeConverter<Object> {
        private final Class<?> clazz;
        private final BiFunction<String, CliContext, Either<String, ?>> parser;
        private final Ref<CliContext> ctxRef;

        @SuppressWarnings("unchecked")
        public Class<Object> clazz() {
            return (Class<Object>) clazz;
        }

        @Override
        public Object convert(String value) {
            return parser.apply(value, ctxRef.get()).getRight((msg) -> {
                throw new TypeConversionException(msg);
            });
        }
    }

    public static class ContextlessTypeConverter extends ContextualTypeConverter {
        public ContextlessTypeConverter(Class<?> clazz, Function<String, Either<String, ?>> parser) {
            super(clazz, (v, _) -> parser.apply(v), new Ref<>((_) -> null));
        }
    }

    private static class Misc {
        public static Either<String, ?> parsePath(String value, CliContext ctx) {
            return Either.pure(ctx.path(value));
        }
    }
}
