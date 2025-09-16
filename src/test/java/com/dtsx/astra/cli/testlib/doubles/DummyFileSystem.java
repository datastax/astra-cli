package com.dtsx.astra.cli.testlib.doubles;

import lombok.experimental.Delegate;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

public class DummyFileSystem extends FileSystem {
    public static DummyFileSystem INSTANCE = new DummyFileSystem();

    private DummyFileSystem() {}

    @Override
    public void close() {}

    @Override
    public FileSystemProvider provider() {
        return shouldNotHaveCalledMethod();
    }

    @Override
    public boolean isOpen() {
        return shouldNotHaveCalledMethod();
    }

    @Override
    public boolean isReadOnly() {
        return shouldNotHaveCalledMethod();
    }

    @Override
    public String getSeparator() {
        return shouldNotHaveCalledMethod();
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return shouldNotHaveCalledMethod();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return shouldNotHaveCalledMethod();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return shouldNotHaveCalledMethod();
    }

    @SuppressWarnings("NullableProblems")
    public class DummyPath implements Path {
        @Delegate(excludes = Exclude.class)
        private final Path delegate = Paths.get("<fake_path>");

        private interface Exclude {
            FileSystem getFileSystem();
        }

        @Override
        public @NotNull FileSystem getFileSystem() {
            return DummyFileSystem.this;
        }
    }

    @Override
    public @NotNull Path getPath(@NotNull String first, @NotNull String @NotNull... more) {
        return new DummyPath();
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        return shouldNotHaveCalledMethod();
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return shouldNotHaveCalledMethod();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        return shouldNotHaveCalledMethod();
    }

    private <T> T shouldNotHaveCalledMethod() {
        val error = new IllegalStateException("File system method should not have been called in this test; use jimfs for tests that require file system access");
        error.fillInStackTrace();
        error.printStackTrace();
        throw error;
    }
}
