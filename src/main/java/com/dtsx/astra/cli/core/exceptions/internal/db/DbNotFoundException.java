package com.dtsx.astra.cli.core.exceptions.internal.db;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.Hint;

import java.util.List;
import java.util.UUID;

import static com.dtsx.astra.cli.core.output.ExitCode.DATABASE_NOT_FOUND;

public class DbNotFoundException extends AstraCliException {
    public DbNotFoundException(DbRef dbRef) {
        super(
            DATABASE_NOT_FOUND,
            dbRef.fold(DbNotFoundException::mkIdMsg, DbNotFoundException::mkNameMsg),
            dbRef.fold(DbNotFoundException::mkIdHints, DbNotFoundException::mkNameHints)
        );
    }

    private static String mkIdMsg(UUID id) {
        return """
          @|bold,red Error: A database with ID '%s' could not be found.|@

          Please ensure that:
            - You are using the correct token/organization.
            - You are using the correct database id.
        """.formatted(id);
    }

    private static String mkNameMsg(String name) {
        return """
          @|bold,red Error: A database named '%s' could not be found.|@

          Please ensure that:
            - You are using the correct token/organization.
            - You are using the correct database name.
        """.formatted(name);
    }

    private static List<Hint> mkIdHints(UUID id) {
        return List.of(
            new Hint("List all dbs in your current org", "${cli.name} db list"),
            new Hint("Check your credentials", "${cli.name} config get <profile>")
        );
    }

    private static List<Hint> mkNameHints(String name) {
        return List.of(
            new Hint("List all dbs in your current org", "${cli.name} db list"),
            new Hint("Check your credentials", "${cli.name} config get <profile>"),
            new Hint("Create a new database with the given name", "${cli.name} db create '" + name + "' <options>")
        );
    }
}
