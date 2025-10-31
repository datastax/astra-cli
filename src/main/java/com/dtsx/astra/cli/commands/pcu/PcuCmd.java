package com.dtsx.astra.cli.commands.pcu;

import com.dtsx.astra.cli.commands.pcu.associations.PcuAssociateCmd;
import com.dtsx.astra.cli.commands.pcu.associations.PcuAssociationListCmd;
import com.dtsx.astra.cli.commands.pcu.associations.PcuAssociationTransferCmd;
import com.dtsx.astra.cli.commands.pcu.associations.PcuDisassociateCmd;
import com.dtsx.astra.cli.core.docs.AliasForSubcommand;
import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "pcu",
    description = "Manage your PCU groups and associations",
    subcommands = {
        PcuListCmd.class,
        PcuGetCmd.class,
        PcuStatusCmd.class,
        PcuParkCmd.class,
        PcuUnparkCmd.class,
        PcuCreateCmd.class,
        PcuUpdateCmd.class,
        PcuDeleteCmd.class,
        PcuAssociateCmd.class,
        PcuDisassociateCmd.class,
        PcuAssociationListCmd.class,
        PcuAssociationTransferCmd.class,
    }
)
@Example(
    comment = "List all your PCU groups",
    command = "${cli.name} pcu"
)
@AliasForSubcommand(PcuListCmd.class)
public final class PcuCmd extends PcuListImpl {}
