package com.dtsx.astra.cli.operations.db.dataapi.runners;

import com.dtsx.astra.cli.operations.db.dataapi.DbDataAPIExecOperation.ExecContext;

import java.nio.file.Path;
import java.util.List;

public interface DataAPIClientRunner {
    void installDeps(Path cacheDir, List<String> packages);

    String createInitScript(ExecContext ctx, String extraCode);
    List<String> createInitVars(ExecContext ctx);

    ProcessBuilder executeCmd(Path cacheDir, String initScript, List<String> extraArgs, boolean isRepl);
}
