package com.dtsx.astra.cli.core.exceptions.internal.misc;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;

import static com.dtsx.astra.cli.utils.MiscUtils.isWindows;

public class WindowsUnsupportedException extends AstraCliException {
    public WindowsUnsupportedException() {
        super("This command is not supported on Windows. Please use a Linux or macOS environment.");
    }

    public static <T> T throwIfWindows() {
        if (isWindows()) {
            throw new WindowsUnsupportedException();
        }
        return null;
    }
}
