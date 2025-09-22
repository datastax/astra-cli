package com.dtsx.astra.cli.core.exceptions.internal.misc;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;

import static com.dtsx.astra.cli.core.output.ExitCode.PLATFORM_ISSUE;

public class WindowsUnsupportedException extends AstraCliException {
    public WindowsUnsupportedException() {
        super(PLATFORM_ISSUE, "This command is not supported on Windows. Please use a Linux or macOS environment.");
    }

    public static <T> T throwIfWindows(CliContext ctx) {
        if (ctx.isWindows()) {
            throw new WindowsUnsupportedException();
        }
        return null;
    }
}
