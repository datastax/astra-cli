package com.dtsx.astra.cli.core.config;

import com.dtsx.astra.cli.core.parsers.ini.Ini.IniSection;

public record InvalidProfile(IniSection section, String issue) {}
