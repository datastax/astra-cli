package com.dtsx.astra.cli.utils;

import com.dtsx.astra.cli.core.CliContext;
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
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@UtilityClass
public class FileUtils {
    public Optional<Path> getCurrentBinaryPath() {
        return ProcessHandle.current().info().command()
            .filter((_) -> ImageInfo.inImageCode())
            .map(Path::of)
            .map(FileUtils::tryToRealPath);
    }

    public Path tryToRealPath(Path path) {
        try {
            return path.toRealPath();
        } catch (IOException e) {
            return path.toAbsolutePath();
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
    public static Path downloadFile(String urlStr, Path installDir, @Nullable String fileName) {
        if (fileName == null) {
            fileName = installDir.getFileSystem().getPath(new URI(urlStr).getPath()).getFileName().toString();
        }

        val targetPath = installDir.resolve(fileName);

        val urlConnection = new URI(urlStr).toURL().openConnection();
        val buffer = new byte[8192];

        @Cleanup val bis = new BufferedInputStream(urlConnection.getInputStream());
        @Cleanup val fos = Files.newOutputStream(targetPath);

        int count;
        while ((count = bis.read(buffer)) != -1) {
            fos.write(buffer, 0, count);
        }

        return targetPath;
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
        val outputDir = tarFile.getParent();

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
