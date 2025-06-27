package com.dtsx.astra.cli.core.help;

import lombok.experimental.UtilityClass;
import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.Model.UsageMessageSpec;

import java.util.ArrayList;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

@UtilityClass
public class DescriptionNewlineRenderer {
    private final String SECTION_KEY = "descriptionNewline";

    public void installRenderer(CommandLine cmd) {
        val index = cmd.getHelpSectionKeys().indexOf(UsageMessageSpec.SECTION_KEY_DESCRIPTION);

        if (index < 0) {
            return;
        }

        cmd.getHelpSectionMap().put(SECTION_KEY, (_) -> {
            return NL;
        });

        cmd.setHelpSectionKeys(
            new ArrayList<>(cmd.getHelpSectionKeys()) {{ add(index + 1, SECTION_KEY); }}
        );
    }
}
