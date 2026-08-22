package com.dtsx.astra.cli.core.config;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileSource;
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
import lombok.*;
import org.apache.commons.io.file.PathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;

import static com.dtsx.astra.cli.core.config.ProfileName.DEFAULT;
import static com.dtsx.astra.cli.core.output.ExitCode.FILE_ISSUE;
import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;
import static com.dtsx.astra.sdk.utils.AstraEnvironment.LOCAL_NAME;
import static com.dtsx.astra.sdk.utils.AstraEnvironment.PROD;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AstraConfig {
    public static final String TOKEN_KEY = "ASTRA_DB_APPLICATION_TOKEN";
    public static final String ENV_KEY = "ASTRA_ENV";
    public static final String LOCAL_ENDPOINT_KEY = "ASTRA_LOCAL_ENDPOINT";

    private final CliContext ctx;
    @NonNull private IniFile backingIniFile;

    @Getter
    private final Path backingFile;

    public static Path resolveDefaultAstraConfigFile(CliContext ctx) {
        return ctx.properties().rcFileLocations(ctx.isWindows()).preferred(ctx);
    }

    public static AstraConfig readAstraConfigFile(CliContext ctx, @Nullable Path maybePath, boolean createIfNotExists) {
        val path = resolvePath(ctx, maybePath, createIfNotExists);

        try {
            val iniFile = IniFile.readFile(path);
            val config = new AstraConfig(ctx, iniFile, path);
            config.profiles(); // validate all sections eagerly and populate cache
            return config;
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

    private static Profile mkProfileFromSection(IniSection section, Path configFile) {
        val profileName = ProfileName.parse(section.name())
            .getRight(msg ->
                new AstraConfigFileException(invalidProfileMsg(section.name(), "Error parsing profile name: " + msg), configFile)
            );

        val tokenStr = section.lookupKey(TOKEN_KEY)
            .orElseThrow(() ->
                new AstraConfigFileException(invalidProfileMsg(section.name(), "Missing required key @'!" + TOKEN_KEY + "!@"), configFile)
            );

        val token = AstraToken.parse(tokenStr)
            .getRight(msg ->
                new AstraConfigFileException(invalidProfileMsg(section.name(), "Error parsing @'!" + TOKEN_KEY + "!@: " + msg), configFile)
            );

        val env = lookupEnvironment(section)
            .getRight(msg ->
                new AstraConfigFileException(invalidProfileMsg(section.name(), msg), configFile)
            );

        return new Profile(Optional.of(profileName), token, env);
    }

    private static String invalidProfileMsg(String profileName, String issue) {
        return trimIndent("""
          Failed to parse the profile @'!%s!@:
        
          "%s"
        
          You can fix this by manually editing the configuration file to resolve the issue.
        
          Use @'!${cli.name} config path!@ to get the path to the configuration file.
        """.formatted(profileName, issue));
    }

    private static Either<String, AstraEnvironment> lookupEnvironment(IniSection section) {
        val rawEnv = section.lookupKey(ENV_KEY).orElse(PROD.name());

        if (!rawEnv.equalsIgnoreCase(LOCAL_NAME)) {
            try {
                return Either.pure(AstraEnvironment.valueOf(rawEnv));
            } catch (IllegalArgumentException e) {
                return Either.left("Error parsing @'!" + ENV_KEY + "!@: Got '" + rawEnv + "', expected one of (" + String.join("|", AstraEnvironment.allValuesLower()) + ")");
            }
        }

        val endpoint = section.lookupKey(LOCAL_ENDPOINT_KEY);

        if (endpoint.isEmpty()) {
            return Either.left("Using a LOCAL environment requires @'!" + LOCAL_ENDPOINT_KEY + "!@ to be set");
        }

        return Either.pure(AstraEnvironment.local(endpoint.get()));
    }

    private List<Profile> cachedProfiles;

    public List<Profile> profiles() {
        if (cachedProfiles == null) {
            cachedProfiles = backingIniFile.getSections().stream()
                .map(section -> mkProfileFromSection(section, backingFile))
                .toList();
        }
        return cachedProfiles;
    }

    public boolean profileExists(ProfileName profileName) {
        return profiles().stream()
            .anyMatch(p -> p.nameOrDefault().equals(profileName));
    }

    public Optional<Profile> lookupProfile(ProfileName profileName) {
        val matching = profiles().stream()
            .filter(p -> p.nameOrDefault().equals(profileName))
            .toList();

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

        return Optional.of(matching.getFirst());
    }

    public Optional<IniSection> lookupSection(String sectionName) {
        return backingIniFile.getSections().stream()
            .filter(s -> s.name().equals(sectionName))
            .findFirst();
    }

    public void modify(Consumer<ProfileModificationCtx> consumer) {
        val modCtx = new ProfileModificationCtx();
        consumer.accept(modCtx);

        // apply all transforms to a working copy of the ini so the original stays untouched until write succeeds
        val newIni = backingIniFile.copy();
        for (val transform : modCtx.iniTransforms) {
            transform.accept(newIni);
        }

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile(".astrarc_tmp", null);
            newIni.writeToFile(tempFile);
            FileUtils.atomicMove(tempFile, backingFile);
            tempFile = null;
        } catch (IOException e) {
            throw new AstraConfigFileException("Error writing config file: " + e.getMessage(), backingFile);
        } finally {
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); } catch (IOException ignored) {}
            }
        }

        backingIniFile = newIni;
        cachedProfiles = null;

        for (val cacheOp : modCtx.cacheOps) {
            cacheOp.run();
        }
    }

    public class ProfileModificationCtx {
        private final List<Consumer<IniFile>> iniTransforms = new ArrayList<>();
        private final List<Runnable> cacheOps = new ArrayList<>();

        public void createProfile(ProfileName name, AstraToken token, AstraEnvironment env) {
            createProfile(name, token, env, Optional.empty());
        }

        public void createProfile(ProfileName name, AstraToken token, AstraEnvironment env, Optional<String> localEndpoint) {
            if (env.name().equalsIgnoreCase(LOCAL_NAME) && localEndpoint.isEmpty()) {
                throw new AstraCliException(FILE_ISSUE, "LOCAL environment requires @'!" + LOCAL_ENDPOINT_KEY + "!@ to be set");
            }

            iniTransforms.add(ini -> ini.addSection(name.unwrap(), new TreeMap<>() {{
                put(TOKEN_KEY, token.unsafeUnwrap());
                if (env != PROD) {
                    put(ENV_KEY, env.name());
                }
                if (env.name().equalsIgnoreCase(LOCAL_NAME) && localEndpoint.isPresent()) {
                    put(LOCAL_ENDPOINT_KEY, localEndpoint.get());
                }
            }}));
        }

        public void setDefault(ProfileName src) {
            copyProfile(src, DEFAULT);
        }

        public void renameProfile(ProfileName oldName, ProfileName newName) {
            copyProfile(oldName, newName);
            deleteProfile(oldName);
        }

        private void copyProfile(ProfileName src, ProfileName target) {
            iniTransforms.add((ini) -> {
                val srcSection = ini.getSections().stream()
                    .filter(s -> s.name().equals(src.unwrap()))
                    .findFirst()
                    .orElseThrow(() -> new AstraConfigFileException("Source profile '" + src + "' not found", backingFile));

                ini.deleteSection(target.unwrap());
                ini.addSection(target.unwrap(), srcSection);
            });
            cacheOps.add(() -> copyCacheDir(src, target));
        }

        public void deleteProfile(ProfileName profileName) {
            iniTransforms.add(ini -> ini.deleteSection(profileName.unwrap()));

            cacheOps.add(() ->
                ProfileLinkedCompletionsCache.pathForProfile(ctx, mkSource(profileName)).ifPresent(path -> {
                    try {
                        PathUtils.delete(path);
                    } catch (IOException e) {
                        ctx.log().exception("Error deleting completion cache at " + path, e);
                    }
                })
            );
        }

        private void copyCacheDir(ProfileName src, ProfileName target) {
            val maybeSrcPath = ProfileLinkedCompletionsCache.pathForProfile(ctx, mkSource(src));

            maybeSrcPath.filter(Files::exists).ifPresent(srcPath -> {
                val targetPath = ProfileLinkedCompletionsCache.pathForProfile(ctx, mkSource(target)).orElseThrow();

                try {
                    if (Files.exists(targetPath)) {
                        PathUtils.delete(targetPath);
                    }
                    PathUtils.copyDirectory(srcPath, targetPath);
                } catch (IOException e) {
                    ctx.log().exception("Error copying completion cache from " + src + " to " + target, e);
                }
            });
        }

        private ProfileSource mkSource(ProfileName profileName) {
            return usingDefaultFile()
                ? new ProfileSource.DefaultFile(profileName)
                : new ProfileSource.CustomFile(backingFile, profileName);
        }
    }

    private boolean usingDefaultFile() {
        try {
            return backingFile.toRealPath().equals(resolveDefaultAstraConfigFile(ctx).toRealPath());
        } catch (IOException e) {
            ctx.log().exception("Error resolving real file paths for checking if config file is default", e);
            return false;
        }
    }
}
