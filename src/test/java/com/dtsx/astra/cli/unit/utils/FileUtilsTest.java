package com.dtsx.astra.cli.unit.utils;

import com.dtsx.astra.cli.testlib.Fixtures;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext;
import com.dtsx.astra.cli.testlib.extensions.context.UseTestCtx;
import com.dtsx.astra.cli.utils.FileUtils;
import lombok.SneakyThrows;
import lombok.val;
import net.jqwik.api.*;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

@Group
public class FileUtilsTest {
    @UseTestCtx(fs = "jimfs")
    private TestCliContext ctx;

    @Group
    class downloadFile {
        @Property
        @SneakyThrows
        public void downloads_file_to_given_file_name(@ForAll("fileName") String srcFileName, @ForAll("fileName") String destFileName, @ForAll byte[] content) {
            assumeThat(srcFileName).isNotEqualToIgnoringCase(destFileName);

            val sourceFile = ctx.get().path("root").resolve(srcFileName);
            val destFile = ctx.get().path("root").resolve(destFileName);

            Files.createDirectories(sourceFile.getParent());
            Files.write(sourceFile, content);

            val fileUrl = sourceFile.toUri().toString();
            FileUtils.downloadFile(fileUrl, destFile.getParent(), destFileName);

            assertThat(destFile)
                .hasFileName(destFileName)
                .hasBinaryContent(content)
                .hasSameBinaryContentAs(sourceFile);
        }

        @Property
        @SneakyThrows
        public void downloads_file_to_implicit_file_name(@ForAll("fileName") String fileName, @ForAll byte[] content) {
            val sourceFile = ctx.get().path("src").resolve(fileName);
            val destFile = ctx.get().path("dst").resolve(fileName);

            Files.createDirectories(sourceFile.getParent());
            Files.createDirectories(destFile.getParent());
            Files.write(sourceFile, content);

            val fileUrl = sourceFile.toUri().toString();
            FileUtils.downloadFile(fileUrl, destFile.getParent(), null);

            assertThat(destFile)
                .hasFileName(fileName)
                .hasBinaryContent(content)
                .hasSameBinaryContentAs(sourceFile);
        }

        @Property
        @SneakyThrows
        public void overwrites_any_existing_file(@ForAll("fileName") String srcFileName, @ForAll("fileName") String destFileName, @ForAll byte[] oldContent, @ForAll byte[] newContent) {
            assumeThat(srcFileName).isNotEqualToIgnoringCase(destFileName);

            val sourceFile = ctx.get().path("root").resolve(srcFileName);
            val destFile = ctx.get().path("root").resolve(destFileName);

            Files.createDirectories(sourceFile.getParent());
            Files.write(sourceFile, newContent);
            Files.write(destFile, oldContent);

            assertThat(destFile).hasBinaryContent(oldContent);

            val fileUrl = sourceFile.toUri().toString();
            FileUtils.downloadFile(fileUrl, destFile.getParent(), destFileName);

            assertThat(destFile)
                .hasFileName(destFileName)
                .hasBinaryContent(newContent);
        }

        @Provide
        private Arbitrary<String> fileName() {
            return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(255)
                .alpha()
                .withChars('.', '-', '_')
                .filter(s -> !s.startsWith("."));
        }
    }

    @Group
    class extractArchiveInPlace {
        @Example
        public void extracts_astra_tar_gz_correctly() {
            val path = copyResourceToJimfs(Fixtures.Files.AstraTarGz, ".astra/astra.tar.gz");

            FileUtils.extractTarGzArchiveInPlace(path, ctx.get());

            assertThat(path.resolveSibling("astra"))
                .isDirectory()
                .satisfies(dir -> {
                    assertThat(dir.resolve("bin"))
                        .isDirectory()
                        .satisfies(binDir -> {
                            assertThat(binDir.resolve("astra")).isRegularFile();
                        });
                });
        }

        @Example
        public void extracts_astra_zip_correctly() {
            val path = copyResourceToJimfs(Fixtures.Files.AstraZip, ".astra/astra.zip");

            FileUtils.extractZipArchiveInPlace(path, ctx.get());

            assertThat(path.resolveSibling("astra"))
                .isDirectory()
                .satisfies(dir -> {
                    assertThat(dir.resolve("bin"))
                        .isDirectory()
                        .satisfies(binDir -> {
                            assertThat(binDir.resolve("astra.exe")).isRegularFile();
                        });
                });
        }

        @Example
        public void extracts_cqlsh_tar_gz_correctly() {
            val path = copyResourceToJimfs(Fixtures.Files.CqlshTarGz, ".astra/cqlsh.tar.gz");

            FileUtils.extractTarGzArchiveInPlace(path, ctx.get());

            val root = path.resolveSibling("cqlsh-astra");

            assertThat(root)
                .isDirectory();

            assertThat(root.resolve("bin"))
                .isDirectoryContaining((p) -> List.of("cqlsh", "cqlsh.py", "dsecqlsh.py").contains(p.getFileName().toString()));

            assertThat(root.resolve("pylib"))
                .isDirectory();

            assertThat(root.resolve("zipfiles"))
                .isDirectory();
        }

        @SneakyThrows
        private Path copyResourceToJimfs(URL realFilePathUrl, String jimfsFilePathStr) {
            val jimfsFilePath = ctx.get().path(jimfsFilePathStr);
            Files.createDirectories(jimfsFilePath.getParent());
            Files.copy(Path.of(realFilePathUrl.toURI()), jimfsFilePath);
            assertThat(jimfsFilePath).isRegularFile();
            return jimfsFilePath;
        }
    }
}
