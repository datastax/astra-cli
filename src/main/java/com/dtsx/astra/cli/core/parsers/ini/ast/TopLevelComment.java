package com.dtsx.astra.cli.core.parsers.ini.ast;

import com.dtsx.astra.cli.core.output.AstraColors;
import lombok.val;

import java.util.List;
import java.util.StringJoiner;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

public record TopLevelComment(List<String> comments) implements TopLevelIniNode {
    @Override
    public String render(AstraColors colors) {
        val sj = new StringJoiner(NL);

        for (val comment : comments) {
            sj.add(colors.NEUTRAL_400.use(comment));
        }

        return sj.toString();
    }
}
