package com.dtsx.astra.cli.core.parsers.ini;

import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.parsers.ParsedFile;
import com.dtsx.astra.cli.core.parsers.ini.ast.IniKVPair;
import com.dtsx.astra.cli.core.parsers.ini.ast.IniSection;
import com.dtsx.astra.cli.core.parsers.ini.ast.TopLevelIniNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.*;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

@NoArgsConstructor
@AllArgsConstructor
public class IniFile extends ParsedFile {
    @Getter
    private ArrayList<TopLevelIniNode> nodes;

    public void addSection(String name, Map<String, String> pairs) {
        nodes.add(new IniSection(name, pairs.entrySet().stream().map(e -> new IniKVPair(List.of(), e.getKey(), e.getValue())).toList()));
    }

    public void addSection(String name, IniSection base) {
        nodes.add(new IniSection(name, new ArrayList<>(base.pairs())));
    }

    public void deleteSection(String name) {
        nodes.removeIf((n) -> (n instanceof IniSection s) && s.name().equals(name));
    }

    public List<IniSection> getSections() {
        return nodes.stream()
            .filter(IniSection.class::isInstance)
            .map(IniSection.class::cast)
            .toList();
    }

    @Override
    public String render(AstraColors colors) {
        val sj = new StringJoiner(NL);

        for (val node : nodes) {
            sj.add(node.render(colors));
        }

        return sj.toString();
    }

    public static IniFile readFile(Path path) throws IniParseException, FileNotFoundException {
        return ParsedFile.readFile(path, new IniParser()::parseIniFile);
    }
}
