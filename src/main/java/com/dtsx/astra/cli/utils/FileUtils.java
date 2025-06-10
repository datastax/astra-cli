package com.dtsx.astra.cli.utils;

import com.dtsx.astra.cli.core.exceptions.misc.CannotCreateFileException;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.function.Supplier;

@UtilityClass
public class FileUtils {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void createFileIfNotExists(File file, Supplier<String> errorMessage) {
        try {
            file.createNewFile();
        } catch (Exception e) {
            throw new CannotCreateFileException(file, errorMessage.get(), e);
        }
    }
}
