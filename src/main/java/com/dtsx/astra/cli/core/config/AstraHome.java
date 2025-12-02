package com.dtsx.astra.cli.core.config;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Thunk;
import com.dtsx.astra.cli.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

public class AstraHome {
    private final Supplier<CliContext> ctxSupplier;
    private Thunk<Path> dir;

    public AstraHome(Supplier<CliContext> ctxSupplier) {
        this.ctxSupplier = ctxSupplier;

        this.dir = new Thunk<>(() -> (
           ctx().path(ctx().properties().homeFolderLocations(ctx().isWindows()).preferred())
        ));
    }

    public String root() {
        return dir.get().toString();
    }

    public final UsablePath updateNotifierProperties = new Thunk<>(() -> {
        Path path = dir.get().resolve("upgrade-notifier.properties");
        FileUtils.createFileIfNotExists(path, null);
        return path;
    })::get;

    public final AstraSubfolders dirs = new AstraSubfolders();

    public class AstraSubfolders {
        public final AstraSubfolder scb = new AstraSubfolder("scb");
        public final AstraSubfolder completionsCache = new AstraSubfolder("completions-cache");
        public final AstraSubfolder logs = new AstraSubfolder("logs");

        public AstraSubfolder cqlsh(String version) {
            return new AstraSubfolder("cqlsh-astra@v1", version);
        }

        public AstraSubfolder dsbulk(String version) {
            return new AstraSubfolder("dsbulk@v1", version);
        }

        public AstraSubfolder pulsar(String version) {
            return new AstraSubfolder("pulsar-shell@v1", version);
        }
    }

    public interface UsablePath {
        Path use();
    }

    public class AstraSubfolder implements UsablePath {
        private final Supplier<Path> folder;

        public AstraSubfolder(String... subfolder) {
            this.folder = new Thunk<>(() -> dir.get().resolve(ctx().fs().getPath("", subfolder)));
        }

        public boolean exists() {
            return Files.exists(folder.get());
        }

        public Optional<Path> useIfExists() {
            if (exists()) {
                return Optional.of(use());
            }
            return Optional.empty();
        }

        @Override
        public Path use() {
            FileUtils.createDirIfNotExists(folder.get(), null);
            return folder.get();
        }
    }

    private CliContext ctx() {
        return ctxSupplier.get();
    }
}
