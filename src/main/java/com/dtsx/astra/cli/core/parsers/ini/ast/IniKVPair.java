package com.dtsx.astra.cli.core.parsers.ini.ast;

import com.dtsx.astra.cli.core.output.AstraColors;
import lombok.val;

import java.util.List;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

public record IniKVPair(List<String> comments, String key, String value) implements IniNode {
    @Override
    @SuppressWarnings("DuplicatedCode")
    public String render(AstraColors colors) {
        val sb = new StringBuilder();

        for (val comment : comments) {
            sb.append((colors.NEUTRAL_400.use(comment)));
            sb.append(NL);
        }

        sb.append(colors.BLUE_300.use(key)).append(colors.NEUTRAL_400.use("=")).append(value);

        return sb.toString();
    }
}
