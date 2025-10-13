package com.dtsx.astra.cli.core.docs;

import lombok.val;

import java.util.LinkedHashMap;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

public record NavPage(LinkedHashMap<String, DocsPage> sections) implements Page {
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

        for (var entry : sections.entrySet()) {
            val title = entry.getKey();
            val page = entry.getValue();

            sb.append(NL).append('.').append(title).append(NL);
            mkNavItems(sb, page, 0);
        }

        return sb.append(NL).toString();
    }

    private void mkNavItems(StringBuilder sb, DocsPage page, int depth) {
        if (depth > 0) {
            sb.append("*".repeat(depth)).append(" xref:reference:").append(page.fileName()).append("[]\n");
        }

        for (DocsPage sub : page.subcommands()) {
            mkNavItems(sb, sub, depth + 1);
        }
    }
}
