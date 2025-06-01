package com.dtsx.astra.cli.commands.db;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "get"
)
public class DbGetCmd extends AbstractDbSpecificCmd {
    public enum DbGetKeys {
        id,
        status,
        cloud,
        keyspace,
        keyspaces,
        region,
        regions
    }

    @Option(names = { "-k", "--key" })
    private DbGetKeys key;

    @Override
    public String executeHuman() {
        return "Running 'astra db get'";
    }
}
