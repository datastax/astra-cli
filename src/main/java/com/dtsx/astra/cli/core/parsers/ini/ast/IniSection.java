package com.dtsx.astra.cli.core.parsers.ini.ast;

import com.dtsx.astra.cli.core.output.AstraColors;
import lombok.val;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

public record IniSection(String name, List<IniKVPair> pairs) implements TopLevelIniNode {
    @Override
    public String render(AstraColors colors) {
        val sj = new StringJoiner(NL);

        val header = "[" + name + "]";
        sj.add(colors.PURPLE_300.use(header));

        for (IniKVPair pair : pairs) {
            sj.add(pair.render(colors));
        }

        return sj + NL;
    }

    public Optional<String> lookupKey(String key) {
        for (int i = pairs.size() - 1; i >= 0; i--) {
            if (key.equals(pairs.get(i).key())) {
                return Optional.of(pairs.get(i).value());
            }
        }
        return Optional.empty();
    }
}
