package com.dtsx.astra.cli.core.exceptions.db;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraColors;

import java.util.UUID;

public class DbNotFoundException extends AstraCliException {
    public DbNotFoundException(DbRef dbRef) {
        super(dbRef.fold(DbNotFoundException::mkIdMsg, DbNotFoundException::mkNameMsg));
    }

    private static String mkIdMsg(UUID id) {
        return """
          @|bold,red Error: A database with ID '%s' was not found.|@

          You can:
          - Check your credentials with %s or %s
          - List your org's databases with %s
          - Create a new database with %s
        """.formatted(
            id,
            AstraColors.highlight("astra config list"),
            AstraColors.highlight("astra config get <profile>"),
            AstraColors.highlight("astra db list"),
            AstraColors.highlight("astra db create <name> <options>")
        );
    }

    private static String mkNameMsg(String name) {
        return """
          @|bold,red Error: A database named '%s' was not found.|@

          You can:
          - Check your credentials with %s or %s
          - List your org's databases with %s
          - Create a new database with %s
        """.formatted(
            name,
            AstraColors.highlight("astra config list"),
            AstraColors.highlight("astra config get <profile>"),
            AstraColors.highlight("astra db list"),
            AstraColors.highlight("astra db create '" + name + "' <options>")
        );
    }
}
