package com.dtsx.astra.cli.config;

import com.dtsx.astra.cli.completions.CompletionsCache;
import com.dtsx.astra.cli.completions.ProfileLinkedCompletionsCache;
import com.dtsx.astra.cli.utils.FileUtils;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AstraConfig {
    public static final String ASTRARC_FILE_NAME = ".astrarc-pico";
    public static final String TOKEN_KEY = "ASTRA_DB_APPLICATION_TOKEN";
    public static final String ENV_KEY = "ASTRA_ENV";

    public record Profile(String name, String token, AstraEnvironment env) {}

    @Getter
    private final ArrayList<Profile> profiles;

    private final Ini backingIni;
    private final File backingFile;

    public static AstraConfig readAstraConfigFile(@Nullable File file) {
        if (file == null) {
            file = resolveDefaultAstraConfigFile();

            if (!FileUtils.createFileIfNotExists(file)) {
                throw new IllegalStateException("Cannot create configuration file: " + file.getPath());
            }
        }

        val iniFile = Ini.readIniFile(file);

        val profiles = iniFile.getSections().stream()
            .map((section) -> new Profile(
                section.name(),
                section.lookupKey(TOKEN_KEY).orElseThrow(() -> new NoSuchElementException("Missing " + TOKEN_KEY + " for profile " + section.name())),
                AstraEnvironment.valueOf(section.lookupKey(ENV_KEY).orElse("PROD").toUpperCase())
            ))
            .toList();

        return new AstraConfig(new ArrayList<>(profiles), iniFile, file);
    }

    public static File resolveDefaultAstraConfigFile() {
        return new File(System.getProperty("user.home") + File.separator + ASTRARC_FILE_NAME);
    }

    public void createProfile(String name, String token, AstraEnvironment env) {
        profiles.add(new Profile(name, token, env));

        backingIni.addSection(name, new HashMap<>() {{
            put(TOKEN_KEY, token);

            if (env != AstraEnvironment.PROD) {
                put(ENV_KEY, env.name());
            }
        }});

        backingIni.writeToFile(backingFile);
    }

    public Optional<Profile> lookupProfile(String profileName) {
        return profiles.stream()
            .filter((p) -> p.name().equals(profileName))
            .findFirst();
    }

    public void deleteProfile(String profileName) {
        profiles.removeIf((p) -> p.name().equals(profileName));
        backingIni.deleteSection(profileName);
        ProfileLinkedCompletionsCache.mkInstances(profileName).forEach(CompletionsCache::delete);
        backingIni.writeToFile(backingFile);
    }

    public Ini.IniSection getProfileSection(String profileName) {
        return backingIni.getSections().stream()
            .filter(s -> s.name().equals(profileName))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Profile '" + profileName + "' not found"));
    }
}
