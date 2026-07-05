package com.dtsx.astra.cli.operations.db.dataapi.runners;

import com.dtsx.astra.cli.operations.db.dataapi.DbDataAPIExecOperation.ExecContext;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class JavaScriptRunner implements DataAPIClientRunner {
    @Override
    @SneakyThrows
    public void installDeps(Path cacheDir, List<String> packages) {
        val cmd = new ArrayList<>(List.of("npm", "install", "--no-save", "@datastax/astra-db-ts@latest")) {{
            addAll(packages);
        }};

        new ProcessBuilder(cmd).directory(cacheDir.toFile()).start().waitFor();
    }

    @Override
    public String createInitScript(ExecContext ctx, String extraCode) {
        return """
        const $ = require('@datastax/astra-db-ts');
       
        let client = new $.DataAPIClient('%s');
        let db = client.db('%s');
        
        %s
        %s
        
        let admin = client.admin({ astraEnv: '%s' });
        let dbAdmin = db.admin({ astraEnv: '%s' });

        %s
        """.formatted(
            ctx.token().unsafeUnwrap(),
            ctx.endpoint(),
            ctx.collection().map(c -> "let coll = db.collection(\"" + c + "\");").orElse(""),
            ctx.table().map(t -> "let table = db.table(\"" + t + "\");").orElse(""),
            ctx.env().name().toLowerCase(),
            ctx.env().name().toLowerCase(),
            extraCode
        );
    }

    @Override
    public List<String> createInitVars(ExecContext ctx) {
        return new ArrayList<>(List.of("client", "db", "admin", "dbAdmin")) {{
            ctx.collection().ifPresent(_ -> add("coll"));
            ctx.table().ifPresent(_ -> add("table"));
        }};
    }

    @Override
    public ProcessBuilder executeCmd(Path cacheDir, String initScript, List<String> extraArgs, boolean isRepl) {
        val pb = new ProcessBuilder(new ArrayList<>() {{
            add("node");
            if (isRepl) {
                add("-i");
            }
            add("-e");
            add(initScript);
            addAll(extraArgs);
        }});
        pb.environment().put("NODE_PATH", cacheDir.resolve("node_modules").toString());
        return pb;
    }
}
