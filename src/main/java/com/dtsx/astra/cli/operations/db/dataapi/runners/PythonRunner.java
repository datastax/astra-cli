package com.dtsx.astra.cli.operations.db.dataapi.runners;

import com.dtsx.astra.cli.operations.db.dataapi.DbDataAPIExecOperation.ExecContext;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

@RequiredArgsConstructor
public final class PythonRunner implements DataAPIClientRunner {
    @Override
    @SneakyThrows
    public void installDeps(Path cacheDir, List<String> packages) {
        new ProcessBuilder("python", "-m", "venv", cacheDir.toString()).start().waitFor();

        val pipPath = cacheDir.resolve("bin").resolve("pip").toString();
        val pipCmd = new ArrayList<>(List.of(pipPath, "install", "-U", "astrapy")) {{
            addAll(packages);
        }};

        new ProcessBuilder(pipCmd).start().waitFor();
    }

    @Override
    public String createInitScript(ExecContext ctx, String extraCode) {
        return """
        from astrapy import DataAPIClient
       
        client = DataAPIClient('%s', environment='%s')
        db = client.get_database('%s')
        
        %s
        %s
        
        admin = client.get_admin()
        db_admin = db.get_database_admin()

        %s
        """.formatted(
            ctx.token().unsafeUnwrap(),
            ctx.env().name().toLowerCase(),
            ctx.endpoint(),
            ctx.collections().stream().map(c ->  c + " = db.get_collection(\"" + c + "\")").collect(joining("\n")),
            ctx.tables().stream().map(t -> t + " = db.get_table(\"" + t + "\")").collect(joining("\n")),
            extraCode
        );
    }

    @Override
    public List<String> createInitVars(ExecContext ctx) {
        return new ArrayList<>(List.of("client", "db", "admin", "db_admin")) {{
            addAll(ctx.collections());
            addAll(ctx.tables());
        }};
    }

    @Override
    public ProcessBuilder executeCmd(Path cacheDir, String initScript, List<String> extraArgs, boolean isRepl) {
        return new ProcessBuilder(new ArrayList<>() {{
            add(cacheDir.resolve("bin").resolve("python").toString());
            if (isRepl) {
                add("-i");
            }
            add("-c");
            add(initScript);
            addAll(extraArgs);
        }});
    }
}
