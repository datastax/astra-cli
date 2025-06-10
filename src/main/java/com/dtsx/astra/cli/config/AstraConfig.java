package com.dtsx.astra.cli.config;

import com.dtsx.astra.cli.core.completions.CompletionsCache;
import com.dtsx.astra.cli.core.completions.ProfileLinkedCompletionsCache;
import com.dtsx.astra.cli.config.ini.Ini;
import com.dtsx.astra.cli.config.ini.IniParseException;
import com.dtsx.astra.cli.core.exceptions.config.AstraConfigFileException;
import com.dtsx.astra.cli.utils.FileUtils;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AstraConfig {
    public static final String ASTRARC_FILE_NAME = ".astrarc-pico";
    public static final String TOKEN_KEY = "ASTRA_DB_APPLICATION_TOKEN";
    public static final String ENV_KEY = "ASTRA_ENV";

    public record Profile(ProfileName name, String token, AstraEnvironment env) {}

    @Getter
    private final ArrayList<Profile> profiles;

    private final Ini backingIni;
    private final File backingFile;

    public static AstraConfig readAstraConfigFile(@Nullable File file) {
        if (file == null) {
            file = resolveDefaultAstraConfigFile();
            FileUtils.createFileIfNotExists(file, () -> "");
        }

        try {
            val iniFile = Ini.readIniFile(file);
            val finalFile = file;

            val profiles = iniFile.getSections().stream()
                .map((section) -> {
                    val profileName = ProfileName.mkUnsafe(section.name()); // If it were invalid, it would've been rejected by the Ini parser itself

                    val token = section.lookupKey(TOKEN_KEY)
                        .orElseThrow(() -> new AstraConfigFileException("Given configuration file '" + finalFile.getPath() + "' is missing the " + TOKEN_KEY + " for profile '" + section.name() + "'", null));

                    val env = AstraEnvironment.valueOf(
                        section.lookupKey(ENV_KEY).orElse("PROD").toUpperCase()
                    );

                    return new Profile(profileName, token, env);
                })
                .toList();

            return new AstraConfig(new ArrayList<>(profiles), iniFile, file);
        } catch (IniParseException e) {
            throw new AstraConfigFileException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new AstraConfigFileException("Given configuration file '" + file.getPath() + "' could not be found.", e);
        }
    }

    public static File resolveDefaultAstraConfigFile() {
        return new File(System.getProperty("user.home") + File.separator + ASTRARC_FILE_NAME);
    }

    public void createProfile(ProfileName name, String token, AstraEnvironment env) {
        profiles.add(new Profile(name, token, env));

        backingIni.addSection(name.unwrap(), new HashMap<>() {{
            put(TOKEN_KEY, token);

            if (env != AstraEnvironment.PROD) {
                put(ENV_KEY, env.name());
            }
        }});

        backingIni.writeToFile(backingFile);
    }

    public Optional<Profile> lookupProfile(ProfileName profileName) {
        return profiles.stream()
            .filter((p) -> p.name().equals(profileName))
            .findFirst();
    }

    public void deleteProfile(ProfileName profileName) {
        profiles.removeIf((p) -> p.name().equals(profileName));
        backingIni.deleteSection(profileName.unwrap());
        ProfileLinkedCompletionsCache.mkInstances(profileName).forEach(CompletionsCache::delete);
        backingIni.writeToFile(backingFile);
    }

    public Ini.IniSection getProfileSection(ProfileName profileName) {
        return backingIni.getSections().stream()
            .filter(s -> s.name().equals(profileName.unwrap()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Profile '" + profileName + "' not found"));
    }
}
