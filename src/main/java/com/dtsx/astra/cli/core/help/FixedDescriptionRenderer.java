package com.dtsx.astra.cli.core.help;

import lombok.experimental.UtilityClass;
import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.Help.TextTable;
import picocli.CommandLine.Model.UsageMessageSpec;

@UtilityClass
public class FixedDescriptionRenderer {
    @SuppressWarnings({ "deprecation", "RedundantSuppression" })
    public void installRenderer(CommandLine cmd) {
        val index = cmd.getHelpSectionKeys().indexOf(UsageMessageSpec.SECTION_KEY_DESCRIPTION);

        if (index < 0) {
            return;
        }

        // This is just CommandLine.Help.description(...) inlined, but using the correct color scheme (instead of the default one)
        // The default implementation creates a new color scheme instead of using the configured one, causing styling issues
        cmd.getHelpSectionMap().put(UsageMessageSpec.SECTION_KEY_DESCRIPTION, (help) -> {
            val descLines = help.commandSpec().usageMessage().description();

            if (descLines == null) {
                return "";
            }

            val table = TextTable.forColumnWidths(
                help.colorScheme(),
                help.commandSpec().usageMessage().width()
            );

            table.setAdjustLineBreaksForWideCJKCharacters(
                help.commandSpec().usageMessage().adjustLineBreaksForWideCJKCharacters()
            );
            table.indentWrappedLines = 0;

            for (val line : descLines) {
                table.addRowValues(help.colorScheme().text(line));
            }

            return table.toString();
        });
    }
}
