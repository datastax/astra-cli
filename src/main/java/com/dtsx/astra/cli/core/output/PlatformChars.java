package com.dtsx.astra.cli.core.output;

public class PlatformChars {
    public static String[] spinnerFrames(boolean isWindows) {
        return (isWindows)
            ? new String[]{ "[    ]", "[=   ]", "[==  ]", "[=== ]", "[====]", "[ ===]", "[  ==]", "[   =]" }
            : new String[]{ "⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏" };
    }

    public static String presenceIndicator(boolean isWindows) {
        return (isWindows)
            ? "*"
            : "■";
    }
}
