package com.dtsx.astra.cli.config;

import com.dtsx.astra.cli.CLIProperties;
import com.dtsx.astra.cli.core.completions.ProfileLinkedCompletionsCache;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.config.AstraConfigFileException;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.Hint;
import com.dtsx.astra.cli.core.parsers.ini.Ini;
import com.dtsx.astra.cli.core.parsers.ini.Ini.IniSection;
import com.dtsx.astra.cli.core.parsers.ini.IniParseException;
import com.dtsx.astra.cli.utils.FileUtils;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.core.output.ExitCode.FILE_ISSUE;
import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AstraConfig {
    public static final String TOKEN_KEY = "ASTRA_DB_APPLICATION_TOKEN";
    public static final String ENV_KEY = "ASTRA_ENV";

    public record Profile(Optional<ProfileName> name, AstraToken token, AstraEnvironment env) {
        public boolean isDefault() {
            return name.map(ProfileName::isDefault).orElse(false);
        }

        public boolean isArgsProvided() {
            return name.isEmpty();
        }

        public ProfileName nameOrDefault() {
            return name.orElse(ProfileName.mkUnsafe("<args_provided>"));
        }
    }
    public record InvalidProfile(IniSection section, String issue) {}

    @Getter
    private final ArrayList<Either<InvalidProfile, Profile>> profiles;

    private final Ini backingIni;

    @Getter
    private final File backingFile;

    public List<Profile> getValidatedProfiles() {
        return profiles.stream().map((e) -> e.fold(
            (invalid) -> {
                throw new AstraConfigFileException(invalid.issue, backingFile);
            },
            Function.identity()
        )).toList();
    }

    public static AstraConfig readAstraConfigFile(@Nullable File file, boolean createIfNotExists) {
        val usingDefault = file == null;

        if (usingDefault) {
            file = resolveDefaultAstraConfigFile();
        }

        if (createIfNotExists) {
            FileUtils.createFileIfNotExists(file, null);
        }

        if (!file.exists()) {
            if (usingDefault) {
                throw new AstraCliException(FILE_ISSUE, """
                  @|bold,red Error: The default configuration file (%s) does not exist.|@
                
                  Please run @!astra setup!@ to create the default configuration file, and set up your Astra credentials.
                
                  Alternatively, you can specify a custom configuration file using the @!--config-file!@ option.
                """.formatted(
                    file.getAbsolutePath()
                ), List.of(
                    new Hint("Interactively set up your configuration file", "astra setup"),
                    new Hint("Programmatically set up your configuration file", "astra config create <name> --token <token> [--env <env>]"),
                    new Hint("Example custom config file usage", "astra db list --config-file ~/.custom_astrarc")
                ));
            } else {
                throw new AstraCliException(FILE_ISSUE, """
                  @|bold,red Error: A configuration file at %s could not be found.|@
                
                  Please ensure that the file exists, or create it if it does not.
                """);
            }
        }

        try {
            val iniFile = Ini.readIniFile(file);

            val profiles = iniFile.getSections().stream()
                .map((section) -> {
                    val maybeProfileName = ProfileName.parse(section.name()).bimap(
                        (msg) -> new InvalidProfile(section, "Error parsing profile name " + highlight(section.name()) + ": " + msg),
                        Function.identity()
                    );

                    return maybeProfileName.flatMap((profileName) -> {
                        val token = section.lookupKey(TOKEN_KEY);

                        if (token.isEmpty()) {
                            return Either.left(
                                new InvalidProfile(section, trimIndent("""
                                  The configuration is missing the required %s key for profile %s.
                             
                                  You can fix this by either:
                                  - Manually editing the configuration file to add the key,
                                  - Running %s to delete this profile, or
                                  - Running %s to set the token for this profile.
                                """.formatted(
                                    AstraColors.PURPLE_300.useOrQuote(TOKEN_KEY),
                                    highlight(section.name()),
                                    highlight("astra config delete '" + profileName.unwrap() + "'"),
                                    highlight("astra config create '" + profileName.unwrap() + "' --token <token> [--env <env>] -f")
                                )))
                            );
                        }

                        val env = section.lookupKey(ENV_KEY)
                            .map(String::toUpperCase)
                            .map(AstraEnvironment::valueOf)
                            .orElse(AstraEnvironment.PROD);

                        return AstraToken.parse(token.get()).bimap(
                            (msg) -> new InvalidProfile(section, "Error parsing token for profile " + highlight(profileName.unwrap()) + ": " + msg),
                            (tokenValue) -> new Profile(Optional.of(profileName), tokenValue, env)
                        );
                    });
                })
                .toList();

            return new AstraConfig(new ArrayList<>(profiles), iniFile, file);
        } catch (IniParseException e) {
            throw new AstraConfigFileException(e.getMessage(), file);
        } catch (FileNotFoundException e) {
            throw new AstraConfigFileException("The configuration file could not be found.", file);
        }
    }

    public static File resolveDefaultAstraConfigFile() {
        return new File(System.getProperty("user.home") + File.separator + CLIProperties.rcFileName());
    }

    public boolean profileExists(ProfileName profileName) {
        return profiles.stream().anyMatch(isProfileName(profileName));
    }

    public Optional<AstraConfig.Profile> lookupProfile(ProfileName profileName) {
        val matching = profiles.stream().filter(isProfileName(profileName)).toList();

        if (matching.isEmpty()) {
            return Optional.empty();
        }

        if (matching.size() > 1) {
            throw new AstraConfigFileException(trimIndent("""
              Multiple profiles were found for name %s. Please ensure profile names are unique.
 
              You can fix this by either
              - Manually editing the configuration file to remove duplicates, or
              - Running %s to delete all profiles with this name, then re-create the profile correctly.
            """.formatted(
                highlight(profileName),
                highlight("astra config delete '" + profileName.unwrap() + "'")
            )), backingFile);
        }

        return matching.getFirst().fold(
            (invalid) -> {
                throw new AstraConfigFileException(invalid.issue, backingFile);
            },
            Optional::of
        );
    }

    public class ProfileModificationCtx {
        public void createProfile(ProfileName name, AstraToken token, AstraEnvironment env) {
            profiles.add(Either.right(new Profile(Optional.of(name), token, env)));

            backingIni.addSection(name.unwrap(), new HashMap<>() {{
                put(TOKEN_KEY, token.unwrap());

                if (env != AstraEnvironment.PROD) {
                    put(ENV_KEY, env.name());
                }
            }});
        }

        public void copyProfile(Profile src, ProfileName target) {
            deleteProfile(target);

            profiles.add(Either.right(new Profile(Optional.of(target), src.token(), src.env())));

            val srcSection = backingIni.getSections().stream()
                .filter(s -> s.name().equals(src.nameOrDefault().unwrap()))
                .findFirst()
                .orElseThrow();

            backingIni.addSection(target.unwrap(), srcSection);
        }

        public void deleteProfile(ProfileName profileName) {
            profiles.removeIf(isProfileName(profileName));
            backingIni.deleteSection(profileName.unwrap());
            ProfileLinkedCompletionsCache.mkInstances(profileName).forEach((c) -> c.update((_) -> Set.of()));
        }
    }

    public void modify(Consumer<ProfileModificationCtx> consumer) {
        consumer.accept(new ProfileModificationCtx());
        backingIni.writeToFile(backingFile);
    }

    public Optional<Ini.IniSection> getProfileSection(String sectionName) {
        return backingIni.getSections().stream()
            .filter(s -> s.name().equals(sectionName))
            .findFirst();
    }

    private Predicate<Either<AstraConfig.InvalidProfile, Profile>> isProfileName(ProfileName profileName) {
        return (p) -> p.fold(
            (invalid) -> invalid.section.name().equals(profileName.unwrap()),
            (profile) -> profile.nameOrDefault().equals(profileName)
        );
    }
}
