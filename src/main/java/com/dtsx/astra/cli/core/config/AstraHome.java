package com.dtsx.astra.cli.core.config;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Thunk;
import com.dtsx.astra.cli.core.models.Version;
import com.dtsx.astra.cli.utils.FileUtils;
import lombok.val;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public class AstraHome {
    private final Supplier<CliContext> ctxSupplier;
    private final Thunk<Path> dir;
    private final Thunk<Dirs> dirs;

    public AstraHome(Supplier<CliContext> ctxSupplier) {
        this.ctxSupplier = ctxSupplier;

        this.dir = new Thunk<>(() -> (
           ctx().path(ctx().properties().homeFolderLocations(ctx().isWindows()).preferred())
        ));

        this.dirs = new Thunk<>(Dirs::new);
    }

    public String getDir() {
        return dir.toString();
    }

    public Path useDir() {
        FileUtils.createDirIfNotExists(dir.get(), null);
        return dir.get();
    }

    public Dirs dirs() {
        return dirs.get();
    }

    public class Dirs {
        private final Path SCB = dir.get().resolve("scb");
        private final Path COMPLETIONS_CACHE = dir.get().resolve("completions-cache");
        private final Path LOGS = dir.get().resolve("logs");
        private final Path CQLSH = dir.get().resolve("cqlsh-astra");

        public Path useScb() {
            FileUtils.createDirIfNotExists(SCB, null);
            return SCB;
        }

        public Path useCompletionsCache() {
            FileUtils.createDirIfNotExists(COMPLETIONS_CACHE, null);
            return COMPLETIONS_CACHE;
        }

        public Path useLogs() {
            FileUtils.createDirIfNotExists(LOGS, null);
            return LOGS;
        }

        public Path useCqlsh(Version version) {
            val path = dir.get().resolve("cqlsh-astra@" + version);
            FileUtils.createDirIfNotExists(path, null);
            return CQLSH;
        }

        public Path useDsbulk(Version version) {
            val path = dir.get().resolve("dsbulk@" + version);
            FileUtils.createDirIfNotExists(path, null);
            return path;
        }

        public Path usePulsar(Version version) {
            val path = dir.get().resolve("lunastreaming-shell@" + version);
            FileUtils.createDirIfNotExists(path, null);
            return path;
        }

        public boolean cqlshExists(Version version) {
            val path = dir.get().resolve("cqlsh-astra@" + version);
            return Files.exists(path);
        }

        public boolean dsbulkExists(Version version) {
            val path = dir.get().resolve("dsbulk@" + version);
            return Files.exists(path);
        }

        public boolean pulsarExists(Version version) {
            val path = dir.get().resolve("lunastreaming-shell@" + version);
            return Files.exists(path);
        }
    }

    private CliContext ctx() {
        return ctxSupplier.get();
    }
}
