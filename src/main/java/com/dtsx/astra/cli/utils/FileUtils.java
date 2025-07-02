package com.dtsx.astra.cli.utils;

import com.dtsx.astra.cli.core.exceptions.misc.CannotCreateFileException;
import com.dtsx.astra.cli.core.output.AstraLogger;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;

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

    @SneakyThrows
    public static void downloadFile(String urlStr, String file) {
        val urlConnection = (HttpURLConnection) new URI(urlStr).toURL().openConnection();
        val buffer = new byte[1024];

        @Cleanup val bis = new BufferedInputStream(urlConnection.getInputStream());
        @Cleanup val fis = new FileOutputStream(file);

        int count;

        while ((count = bis.read(buffer, 0, 1024)) != -1) {
            fis.write(buffer, 0, count);
        }
    }

    @SneakyThrows
    public static void extractTarArchiveInPlace(File tarFile) {
        val outputDir = tarFile.getParentFile();

        @Cleanup val fis = new FileInputStream(tarFile);
        @Cleanup val gzIn = new GzipCompressorInputStream(fis);
        @Cleanup val tis = new TarArchiveInputStream(gzIn);

        TarArchiveEntry tarEntry;

        while ((tarEntry = tis.getNextEntry()) != null) {
            File outputFile = new File(outputDir, tarEntry.getName()).getCanonicalFile();

            if (tarEntry.isDirectory()) {
                if (!outputFile.exists() && outputFile.mkdirs()) {
                    AstraLogger.debug("Created directory: %s".formatted(outputFile.getAbsolutePath()));
                }
            } else {
                val parent = outputFile.getParentFile();

                if (parent != null && parent.mkdirs()) {
                    AstraLogger.debug("Created directory: %s".formatted(parent.getAbsolutePath()));
                }

                try (val fos = new FileOutputStream(outputFile)) {
                    IOUtils.copy(tis, fos);
                }
            }
        }
    }
}
