package com.dtsx.astra.cli.core.config;

import com.dtsx.astra.cli.core.parsers.ini.ast.IniSection;

import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;

public record InvalidProfile(IniSection section, String issue) {
    public String message() {
        return trimIndent("""
          Failed to parse the profile @'!%s!@:
        
          "%s"
        
          You can fix this by either:
          - Using @'!astra config (create|delete)!@ to recreate the profile, or
          - Manually editing the configuration file to resolve the issue.
        """.formatted(
            section.name(),
            issue
        ));
    }
}
