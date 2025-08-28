package com.dtsx.astra.cli.core.parsers.ini.ast;

public sealed interface TopLevelIniNode extends IniNode permits IniSection, TopLevelComment {}
