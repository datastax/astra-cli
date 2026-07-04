package com.dtsx.astra.cli.operations.db.dataapi.runners;

import com.dtsx.astra.cli.operations.db.dataapi.DbDataAPIExecOperation.ExecContext;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class PythonRunner implements DataAPIClientRunner {
    @Override
    public String languageName() {
        return "python";
    }

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
            ctx.collection().map(c -> "collection = db.get_collection(\"" + c + "\")").orElse(""),
            ctx.table().map(t -> "table = db.get_table(\"" + t + "\")").orElse(""),
            extraCode
        );
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
