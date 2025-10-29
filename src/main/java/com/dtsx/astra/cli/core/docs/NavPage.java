package com.dtsx.astra.cli.core.docs;

import lombok.val;

import java.util.List;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

public record NavPage(List<DocsPage> topLevelSubcommand) implements Page {
    @Override
    public String fileName() {
        return "partial-nav.adoc";
    }

    @Override
    public String filePath() {
        return fileName();
    }

    @Override
    public String render() {
        val sb = new StringBuilder();

        sb.append(NL).append(".Commands").append(NL);

        for (val subcommand : topLevelSubcommand) {
            mkNavItems(sb, subcommand, 0);
        }

        return sb.append(NL).append(NL).toString();
    }

    private void mkNavItems(StringBuilder sb, DocsPage page, int depth) {
        sb.append("*".repeat(depth + 1)).append(" xref:").append(DocsPage.DIRECTORY).append(":").append(page.fileName()).append("[]").append(NL);

        for (val sub : page.subcommands()) {
            mkNavItems(sb, sub, depth + 1);
        }
    }
}
