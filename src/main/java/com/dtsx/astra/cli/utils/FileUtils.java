package com.dtsx.astra.cli.utils;

import com.dtsx.astra.cli.core.exceptions.misc.CannotCreateFileException;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.file.Files;
import java.util.function.Supplier;

@UtilityClass
public class FileUtils {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void createFileIfNotExists(File file, String extra) {
        try {
            file.createNewFile();
        } catch (Exception e) {
            throw new CannotCreateFileException(file, extra, e);
        }
    }

    public void createDirIfNotExists(File file, String extra) {
        try {
            Files.createDirectories(file.toPath());
        } catch (Exception e) {
            throw new CannotCreateFileException(file, extra, e);
        }
    }

    public String sanitizeFileName(String name) {
        while (name.contains("..")) {
            name = name.replace("..", "__");
        }

        name = name.replace("\\", "_");
        name = name.replace("/", "_");

        name = name.replaceAll("[:*?\"<>|]", "_");
        name = name.replaceAll("\\p{Cntrl}", "_");

        if (name.length() > 100) {
            name = name.substring(0, 100);
        }

        return name;
    }
}
