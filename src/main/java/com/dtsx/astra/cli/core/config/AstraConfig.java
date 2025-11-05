package com.dtsx.astra.cli.core.config;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.completions.ProfileLinkedCompletionsCache;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.config.AstraConfigFileException;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.parsers.ini.IniFile;
import com.dtsx.astra.cli.core.parsers.ini.IniParseException;
import com.dtsx.astra.cli.core.parsers.ini.ast.IniSection;
import com.dtsx.astra.cli.utils.FileUtils;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.dtsx.astra.cli.core.output.ExitCode.FILE_ISSUE;
import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AstraConfig {
    public static final String TOKEN_KEY = "ASTRA_DB_APPLICATION_TOKEN";
    public static final String ENV_KEY = "ASTRA_ENV";

    private final CliContext ctx;

    @Getter
    private final ArrayList<Either<InvalidProfile, Profile>> profiles;

    private final IniFile backingIniFile;

    @Getter
    private final Path backingFile;

    public static Path resolveDefaultAstraConfigFile(CliContext ctx) {
        return ctx.path(ctx.properties().rcFileLocations(ctx.isWindows()).preferred());
    }

    public static AstraConfig readAstraConfigFile(CliContext ctx, @Nullable Path maybePath, boolean createIfNotExists) {
        val path = resolvePath(ctx, maybePath, createIfNotExists);

        try {
            val iniFile = IniFile.readFile(path);

            val profiles = iniFile.getSections().stream()
                .map((section) -> mkProfileFromSection(ctx, section))
                .toList();

            return new AstraConfig(ctx, new ArrayList<>(profiles), iniFile, path);
        } catch (IniParseException e) {
            throw new AstraConfigFileException(e.getMessage(), path);
        } catch (IOException e) {
            throw new AstraConfigFileException("Error opening config file: " + e.getMessage(), path);
        }
    }

    private static @NotNull Path resolvePath(CliContext ctx, @Nullable Path path, boolean createIfNotExists) {
        val usingDefault = path == null;

        if (usingDefault) {
            path = resolveDefaultAstraConfigFile(ctx);
        }

        if (createIfNotExists) {
            FileUtils.createFileIfNotExists(path, null);
        }

        if (!Files.exists(path)) {
            if (usingDefault) {
                throw new AstraCliException(FILE_ISSUE, """
                  @|bold,red Error: The default configuration file (%s) does not exist.|@
                
                  Please run @'!${cli.name} setup!@ to create the default configuration file, and set up your Astra credentials.
                
                  Alternatively, you can specify credentials via the @'!--config-file!@ or @'!--token!@ options.
                """.formatted(path), List.of(
                    new Hint("Interactively set up your configuration file", "${cli.name} setup"),
                    new Hint("Programmatically set up your configuration file", "${cli.name} config create [name] --token <token> [--env <env>]"),
                    new Hint("Example custom config file usage", "${cli.name} db list --config-file ~/.custom_astrarc")
                ));
            } else {
                throw new AstraCliException(FILE_ISSUE, """
                  @|bold,red Error: The given configuration file at %s could not be found.|@
                
                  Please ensure that the file exists, or create it if it does not.
                """.formatted(path));
            }
        }

        return path;
    }

    private static Either<InvalidProfile, Profile> mkProfileFromSection(CliContext ctx, IniSection section) {
        val maybeProfileName = ProfileName.parse(section.name()).bimap(
            (msg) -> new InvalidProfile(section, "Error parsing profile name @'!" + section.name() + "!@: " + msg),
            Function.identity()
        );

        return maybeProfileName.flatMap((profileName) -> {
            val token = section.lookupKey(TOKEN_KEY);

            if (token.isEmpty()) {
                return Either.left(
                    new InvalidProfile(section, "Missing the required key " + ctx.colors().PURPLE_300.useOrQuote(TOKEN_KEY))
                );
            }

            val rawEnv = section.lookupKey(ENV_KEY).orElse("PROD");

            try {
                val env = AstraEnvironment.valueOf(rawEnv.toUpperCase());

                return AstraToken.parse(token.get()).bimap(
                    (msg) -> new InvalidProfile(section, "Error parsing " + ctx.colors().PURPLE_300.useOrQuote(TOKEN_KEY) + ": " + msg),
                    (tokenValue) -> new Profile(Optional.of(profileName), tokenValue, env)
                );
            } catch (IllegalArgumentException e) {
                return Either.left(
                    new InvalidProfile(section, "Error parsing " + ctx.colors().PURPLE_300.useOrQuote(ENV_KEY) + ": Got '" + rawEnv + "', expected one of (prod|dev|test)")
                );
            }
        });
    }

    public List<Profile> profilesValidated() {
        return profiles.stream().map((e) -> e.fold(
            (invalid) -> {
                throw new AstraConfigFileException(invalid.message(), backingFile);
            },
            Function.identity()
        )).toList();
    }

    public boolean profileExists(ProfileName profileName) {
        return profiles.stream().anyMatch(isProfileName(profileName));
    }

    public Optional<Profile> lookupProfile(ProfileName profileName) {
        val matching = profiles.stream().filter(isProfileName(profileName)).toList();

        if (matching.isEmpty()) {
            return Optional.empty();
        }

        if (matching.size() > 1) {
            throw new AstraConfigFileException(trimIndent("""
              Multiple profiles found for name @'!%s!@. Please ensure profile names are unique.
 
              You can fix this by either
              - Manually editing the configuration file to remove duplicates, or
              - Running @'!%s!@ to delete all profiles with this name, then re-create the profile correctly.
            """.formatted(
                profileName,
                "${cli.name} config delete '" + profileName.unwrap() + "'"
            )), backingFile);
        }

        return matching.getFirst().fold(
            (invalid) -> {
                throw new AstraConfigFileException(invalid.message(), backingFile);
            },
            Optional::of
        );
    }

    public Optional<IniSection> lookupSection(String sectionName) {
        return backingIniFile.getSections().stream()
            .filter(s -> s.name().equals(sectionName))
            .findFirst();
    }

    public void modify(Consumer<ProfileModificationCtx> consumer) {
        consumer.accept(new ProfileModificationCtx());
        backingIniFile.writeToFile(backingFile);
    }

    public class ProfileModificationCtx {
        public void createProfile(ProfileName name, AstraToken token, AstraEnvironment env) {
            profiles.add(Either.pure(new Profile(Optional.of(name), token, env)));

            backingIniFile.addSection(name.unwrap(), new TreeMap<>() {{
                put(TOKEN_KEY, token.unsafeUnwrap());

                if (env != AstraEnvironment.PROD) {
                    put(ENV_KEY, env.name());
                }
            }});
        }

        public void copyProfile(Profile src, ProfileName target) {
            val srcSection = backingIniFile.getSections().stream()
                .filter(s -> s.name().equals(src.nameOrDefault().unwrap()))
                .findFirst()
                .orElseThrow();

            deleteProfile(target); // target deleted after getting src in case they are the same

            profiles.add(Either.pure(new Profile(Optional.of(target), src.token(), src.env())));

            backingIniFile.addSection(target.unwrap(), srcSection);
        }

        public void deleteProfile(ProfileName profileName) {
            profiles.removeIf(isProfileName(profileName));
            backingIniFile.deleteSection(profileName.unwrap());
            ProfileLinkedCompletionsCache.mkInstances(ctx, profileName).forEach((c) -> c.setCache(List.of()));
        }
    }

    private Predicate<Either<InvalidProfile, Profile>> isProfileName(ProfileName profileName) {
        return (p) -> p.fold(
            (invalid) -> invalid.section().name().equals(profileName.unwrap()),
            (profile) -> profile.nameOrDefault().equals(profileName)
        );
    }
}
