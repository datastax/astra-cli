package com.dtsx.astra.cli.core.parsers.ini.ast;

import com.dtsx.astra.cli.core.output.AstraColors;

sealed public interface IniNode permits IniKVPair, TopLevelIniNode {
    String render(AstraColors colors);
}
