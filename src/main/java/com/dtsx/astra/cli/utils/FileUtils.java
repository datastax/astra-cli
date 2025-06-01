package com.dtsx.astra.cli.utils;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;

@UtilityClass
public class FileUtils {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean createFileIfNotExists(File file) {
        try {
            file.createNewFile();
            return true;
        } catch (IOException _) {
            return false;
        }
    }
}
