package com.dtsx.astra.cli.core;

import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.datatypes.Ref;
import com.dtsx.astra.cli.core.models.*;
import lombok.RequiredArgsConstructor;
import lombok.val;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

public abstract class TypeConverters {
    public static List<? extends ContextualTypeConverter> mkInstances(Ref<CliContext> ctxRef) {
        return List.of(
            new ContextlessTypeConverter(AstraToken.class, AstraToken::parse),
            new ContextlessTypeConverter(DbRef.class, DbRef::parse),
            new ContextlessTypeConverter(PcuRef.class, PcuRef::parse),
            new ContextlessTypeConverter(RegionName.class, RegionName::parse),
            new ContextlessTypeConverter(DatacenterId.class, DatacenterId::parse),
            new ContextlessTypeConverter(PcuAssocTarget.class, PcuAssocTarget::parse),
            new ContextlessTypeConverter(RoleRef.class, RoleRef::parse),
            new ContextlessTypeConverter(TenantName.class, TenantName::parse),
            new ContextlessTypeConverter(UserRef.class, UserRef::parse),
            new ContextlessTypeConverter(ProfileName.class, ProfileName::parse),
            new ContextlessTypeConverter(Version.class, Version::parse),
            new ContextlessTypeConverter(Duration.class, Misc::parseDuration),
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
        public static Either<String, Path> parsePath(String value, CliContext ctx) {
            return ModelUtils.validateBasics("path", value).map(ctx::path);
        }

        public static Either<String, Duration> parseDuration(String value) {
            return ModelUtils.trimAndValidateBasics("Duration", value).flatMap((duration) -> {
                if (duration.startsWith("-")) {
                    return Either.left("Duration may not be negative");
                }

                if (duration.contains("P") || duration.contains("p")) {
                    try {
                        return Either.pure(Duration.parse(duration));
                    } catch (Exception e) {
                        return Either.left("Invalid ISO-8601 duration: " + e.getMessage());
                    }
                }

                val pattern = Pattern.compile("^+?(\\d+)(ms|s|m|h)?$");
                val matcher = pattern.matcher(duration);

                if (matcher.find()) {
                    val number = Long.parseLong(matcher.group(1));
                    val unit = matcher.group(2);

                    return switch (unit) {
                        case "s" -> Either.pure(Duration.ofSeconds(number));
                        case null -> Either.pure(Duration.ofSeconds(number));
                        case "ms" -> Either.pure(Duration.ofMillis(number));
                        case "m" -> Either.pure(Duration.ofMinutes(number));
                        case "h" -> Either.pure(Duration.ofHours(number));
                        case "d" -> Either.left("Are you sure you want to wait days...? If so, please use ISO-8601 format instead, and pat yourself on your back for your patience.");
                        default -> Either.left("Invalid duration unit: " + unit + "; expected one of (ms|s|m|h) (or none for seconds)");
                    };
                } else {
                    return Either.left("Expected duration to be of the form <number><ms|s|m|h> or ISO-8601 format");
                }
            });
        }
    }
}
