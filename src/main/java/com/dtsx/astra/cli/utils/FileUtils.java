package com.dtsx.astra.cli.utils;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.misc.CannotCreateFileException;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.graalvm.nativeimage.ImageInfo;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static com.dtsx.astra.cli.core.output.ExitCode.FILE_ISSUE;

@UtilityClass
public class FileUtils {
    public Optional<Path> getCurrentBinaryPath() {
        return ProcessHandle.current().info().command()
            .filter((_) -> ImageInfo.inImageCode())
            .map(Path::of)
            .map(FileUtils::toRealPath);
    }

    public Path toRealPath(Path path) {
        try {
            return path.toRealPath();
        } catch (IOException e) {
            throw new AstraCliException(FILE_ISSUE, """
              @|bold,red Failed to resolve the canonical path for @|underline %s|@.|@
            
              This might be due to missing files, permission issues, or symbolic link problems.
            
              Error: '%s'
            """.formatted(path, e.getMessage()));
        }
    }

    public void createFileIfNotExists(Path path, @Nullable String extra) {
        try {
            Files.createDirectories(path.getParent());
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
    public static void extractTarGzArchiveInPlace(Path tarFile, CliContext ctx) {
        extractArchiveInPlace(tarFile, ctx, (is) -> new TarArchiveInputStream(new GzipCompressorInputStream(is)));
    }

    @SneakyThrows
    public static void extractZipArchiveInPlace(Path zipFile, CliContext ctx) {
        extractArchiveInPlace(zipFile, ctx, ZipArchiveInputStream::new);
    }

    @FunctionalInterface
    private interface MkArchiveInputStream<IS extends ArchiveInputStream<?>> {
        IS apply(InputStream is) throws IOException;
    }

    @SneakyThrows
    private static <A extends ArchiveEntry, IS extends ArchiveInputStream<A>> void extractArchiveInPlace(Path tarFile, CliContext ctx, MkArchiveInputStream<IS> mkArchiveInputStream) {
        val outputDir = tarFile.getParent().getParent();

        @Cleanup val fis = Files.newInputStream(tarFile);
        @Cleanup val ais = mkArchiveInputStream.apply(fis);

        A tarEntry;

        while ((tarEntry = ais.getNextEntry()) != null) {
            val entryPath = outputDir.resolve(tarEntry.getName()).normalize();

            if (tarEntry.isDirectory()) {
                if (Files.notExists(entryPath)) {
                    Files.createDirectories(entryPath);
                    ctx.log().debug("Created directory: %s".formatted(entryPath));
                }
            } else {
                val parent = entryPath.getParent();

                if (parent != null && Files.notExists(parent)) {
                    Files.createDirectories(parent);
                    ctx.log().debug("Created directory: %s".formatted(parent));
                }

                try (val fos = Files.newOutputStream(entryPath)) {
                    IOUtils.copy(ais, fos);
                }
            }
        }
    }
}
