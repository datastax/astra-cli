package com.dtsx.astra.cli.commands.streaming;

import picocli.CommandLine.Command;

@Command(
    name = "list",
    description = "List your Astra streaming tenants"
)
public class StreamingListCmd extends StreamingListImpl {}
