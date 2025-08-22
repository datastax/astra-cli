package com.dtsx.astra.cli.core.output;

import static com.dtsx.astra.cli.core.CliEnvironment.isWindows;

public class PlatformChars {
    public static final String[] SPINNER_FRAMES = isWindows()
        ? new String[]{"[    ]", "[=   ]", "[==  ]", "[=== ]", "[====]", "[ ===]", "[  ==]", "[   =]"}
        : new String[]{ "⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏" };

    public static final String PRESENCE_INDICATOR = isWindows()
        ? "*"
        : "■";
}
