package com.dtsx.astra.cli.core.config;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileSource;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.credentials.CredentialStorageFactory;
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
    public static final String CS_TOKEN_KEY = "ASTRA_CS_TOKEN";
    public static final String CS_ORG_ID_KEY = "ASTRA_CS_ORG_ID";
    public static final String CS_ORG_NAME_KEY = "ASTRA_CS_ORG_NAME";
    public static final String CS_ROLE_ID_KEY = "ASTRA_CS_ROLE_ID";
    public static final String CS_ROLE_NAME_KEY = "ASTRA_CS_ROLE_NAME";
    public static final String ENV_KEY = "ASTRA_ENV";
    public static final String LOCAL_ENDPOINT_KEY = "ASTRA_LOCAL_ENDPOINT";
    public static final String CS_STORAGE_KEY = "ASTRA_CS_STORAGE";

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
            FileUtils.setOwnerReadWrite(backingFile);
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
            createProfile(name, token, env, Optional.empty(), Optional.empty());
        }

        public void createProfile(ProfileName name, AstraToken token, AstraEnvironment env, Optional<String> localEndpoint) {
            createProfile(name, token, env, localEndpoint, Optional.empty());
        }

        public void createProfile(ProfileName name, AstraToken token, AstraEnvironment env, Optional<String> localEndpoint, Optional<AstraToken> csToken) {
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
                csToken.ifPresent(cs -> put(CS_TOKEN_KEY, cs.unsafeUnwrap()));
            }}));
        }

        /**
         * Adds or updates the {@code ASTRA_CS_TOKEN} key in an existing profile section.
         * Call this after {@link #createProfile} to store the AstraCS token provisioned
         * by the headless-agent flow.  If the profile does not exist this is a no-op.
         */
        public void setCsToken(ProfileName name, AstraToken csToken) {
            setCsToken(name, csToken, !(CredentialStorageFactory.get() instanceof com.dtsx.astra.cli.core.credentials.FileFallbackStorage));
        }

        public void setCsToken(ProfileName name, AstraToken csToken, boolean useKeychain) {
            var manager = useKeychain ? CredentialStorageFactory.get() : new com.dtsx.astra.cli.core.credentials.FileFallbackStorage();
            String account = name.unwrap() + "." + CS_TOKEN_KEY;
            boolean storedInKeychain = useKeychain && !(manager instanceof com.dtsx.astra.cli.core.credentials.FileFallbackStorage)
                && manager.store(CredentialStorageFactory.SERVICE_NAME, account, csToken.unsafeUnwrap());

            iniTransforms.add(ini -> {
                val existing = ini.getSections().stream()
                    .filter(s -> s.name().equals(name.unwrap()))
                    .findFirst();
                if (existing.isEmpty()) return;

                val oldPairs = new java.util.ArrayList<>(existing.get().pairs());
                // Remove any prior ASTRA_CS_TOKEN and ASTRA_CS_STORAGE entries.
                oldPairs.removeIf(p -> CS_TOKEN_KEY.equals(p.key()) || CS_STORAGE_KEY.equals(p.key()));

                if (storedInKeychain) {
                    // Store a marker instead of the raw secret
                    oldPairs.add(new com.dtsx.astra.cli.core.parsers.ini.ast.IniKVPair(java.util.List.of(), CS_STORAGE_KEY, "keychain"));
                    System.out.println("[astra] Token stored securely in " + manager.name());
                } else {
                    // Fallback: write plaintext (existing behavior)
                    oldPairs.add(new com.dtsx.astra.cli.core.parsers.ini.ast.IniKVPair(java.util.List.of(), CS_TOKEN_KEY, csToken.unsafeUnwrap()));
                }

                ini.deleteSection(name.unwrap());
                ini.addSection(new com.dtsx.astra.cli.core.parsers.ini.ast.IniSection(name.unwrap(), oldPairs));
            });
        }

        /**
         * Adds or updates the four org/role metadata keys in an existing profile section.
         * Call this after {@link #setCsToken} to persist the display names and IDs used
         * when minting the CS token — needed by {@code auth switch} to list existing tokens.
         * If the profile does not exist this is a no-op.
         */
        public void setOrgRoleMeta(ProfileName name,
                                   String orgId, String orgName,
                                   String roleId, String roleName) {
            iniTransforms.add(ini -> {
                val existing = ini.getSections().stream()
                    .filter(s -> s.name().equals(name.unwrap()))
                    .findFirst();
                if (existing.isEmpty()) return;

                val oldPairs = new java.util.ArrayList<>(existing.get().pairs());
                val metaKeys = java.util.Set.of(CS_ORG_ID_KEY, CS_ORG_NAME_KEY, CS_ROLE_ID_KEY, CS_ROLE_NAME_KEY);
                oldPairs.removeIf(p -> metaKeys.contains(p.key()));
                oldPairs.add(new com.dtsx.astra.cli.core.parsers.ini.ast.IniKVPair(java.util.List.of(), CS_ORG_ID_KEY, orgId));
                oldPairs.add(new com.dtsx.astra.cli.core.parsers.ini.ast.IniKVPair(java.util.List.of(), CS_ORG_NAME_KEY, orgName));
                oldPairs.add(new com.dtsx.astra.cli.core.parsers.ini.ast.IniKVPair(java.util.List.of(), CS_ROLE_ID_KEY, roleId));
                oldPairs.add(new com.dtsx.astra.cli.core.parsers.ini.ast.IniKVPair(java.util.List.of(), CS_ROLE_NAME_KEY, roleName));

                ini.deleteSection(name.unwrap());
                ini.addSection(new com.dtsx.astra.cli.core.parsers.ini.ast.IniSection(name.unwrap(), oldPairs));
            });
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

    /**
     * Represents a stored CS token found in a profile, along with the org/role metadata
     * that was saved when the token was minted.  Used by {@code auth switch} to present
     * the user's existing tokens as selectable options without a network call.
     */
    public record StoredCsToken(
        ProfileName profileName,
        String csToken,
        String orgId,
        String orgName,
        String roleId,
        String roleName
    ) {
        /** Display label: "OrgName — RoleName  (profile: profileName)". */
        public String label() {
            return orgName + " \u2014 " + roleName + "  (profile: " + profileName.unwrap() + ")";
        }
    }

    /**
     * Returns every profile that has both an {@code ASTRA_CS_TOKEN} and the four org/role
     * metadata keys.  Profiles that are missing any of the five keys are skipped silently —
     * they were created before PKCE-02 and must go through a fresh {@code login} to populate
     * the metadata before they appear here.
     */
    public List<StoredCsToken> readStoredCsTokens() {
        val result = new java.util.ArrayList<StoredCsToken>();
        for (val section : backingIniFile.getSections()) {
            String csToken = section.lookupKey(CS_TOKEN_KEY).orElse(null);
            val orgId   = section.lookupKey(CS_ORG_ID_KEY).orElse(null);
            val orgName = section.lookupKey(CS_ORG_NAME_KEY).orElse(null);
            val roleId  = section.lookupKey(CS_ROLE_ID_KEY).orElse(null);
            val roleName = section.lookupKey(CS_ROLE_NAME_KEY).orElse(null);

            // If the token was stored in the OS keychain, retrieve it from there.
            String storageMode = section.lookupKey(CS_STORAGE_KEY).orElse(null);
            if ("keychain".equals(storageMode)) {
                String account = section.name() + "." + CS_TOKEN_KEY;
                csToken = CredentialStorageFactory.get().retrieve(CredentialStorageFactory.SERVICE_NAME, account);
            }

            if (csToken == null || orgId == null || orgName == null || roleId == null || roleName == null) {
                continue;
            }
            val profileName = ProfileName.parse(section.name()).fold(__ -> null, pn -> pn);
            if (profileName == null) continue;

            result.add(new StoredCsToken(profileName, csToken, orgId, orgName, roleId, roleName));
        }
        return List.copyOf(result);
    }
}
