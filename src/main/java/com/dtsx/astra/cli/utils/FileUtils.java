package com.dtsx.astra.cli.utils;

import com.dtsx.astra.cli.core.exceptions.internal.misc.CannotCreateFileException;
import com.dtsx.astra.cli.core.output.AstraLogger;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class FileUtils {
    public void createFileIfNotExists(Path path, @Nullable String extra) {
        try {
            Files.createFile(path);
        } catch (FileAlreadyExistsException _) {
           // whatever
        } catch (Exception e) {
            throw new CannotCreateFileException(path, extra, e);
        }
    }

    public void createDirIfNotExists(Path path, @Nullable String extra) {
        try {
            Files.createDirectories(path);
        } catch (Exception e) {
            throw new CannotCreateFileException(path, extra, e);
        }
    }
    
    @SneakyThrows
    public static void downloadFile(String urlStr, Path path) {
        val urlConnection = (HttpURLConnection) new URI(urlStr).toURL().openConnection();
        val buffer = new byte[1024];

        @Cleanup val bis = new BufferedInputStream(urlConnection.getInputStream());
        @Cleanup val fis = Files.newOutputStream(path);

        int count;

        while ((count = bis.read(buffer, 0, 1024)) != -1) {
            fis.write(buffer, 0, count);
        }
    }

    @SneakyThrows
    public static void extractTarArchiveInPlace(Path tarFile) {
        val outputDir = tarFile.getParent();

        @Cleanup val fis = Files.newInputStream(tarFile);
        @Cleanup val gzIn = new GzipCompressorInputStream(fis);
        @Cleanup val tis = new TarArchiveInputStream(gzIn);

        TarArchiveEntry tarEntry;

        while ((tarEntry = tis.getNextEntry()) != null) {
            val entryPath = outputDir.resolve(tarEntry.getName()).normalize();

            if (tarEntry.isDirectory()) {
                if (Files.notExists(entryPath)) {
                    Files.createDirectories(entryPath);
                    AstraLogger.debug("Created directory: %s".formatted(entryPath));
                }
            } else {
                val parent = entryPath.getParent();

                if (parent != null && Files.notExists(parent)) {
                    Files.createDirectories(parent);
                    AstraLogger.debug("Created directory: %s".formatted(parent));
                }

                try (val fos = Files.newOutputStream(entryPath)) {
                    IOUtils.copy(tis, fos);
                }
            }
        }
    }
}
