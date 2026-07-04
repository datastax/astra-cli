package com.dtsx.astra.cli.utils;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import lombok.val;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static com.dtsx.astra.cli.core.output.ExitCode.IO_ISSUE;
import static com.dtsx.astra.cli.core.output.ExitCode.PLATFORM_ISSUE;

public class ShellUtils {
    public static void copyToClipboard(CliContext ctx, String content) {
        val os = ctx.env().platform().os();

        try {
            val pb = switch (os) {
                case WINDOWS -> copyToClipboardWindows();
                case LINUX -> copyToClipboardLinux();
                case MAC -> copyToClipboardMac();
                case OTHER -> throw new AstraCliException(PLATFORM_ISSUE, """
              @|bold,red Clipboard copy is not supported on your OS (%s)|@
            """.formatted(os));
            };

            val process = pb.redirectErrorStream(true).start();

            try (val writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                writer.write(content);
                writer.flush();
            }

            if (process.waitFor() != 0) {
                throw new AstraCliException(IO_ISSUE, """
                  @|bold,red Failed to copy token to clipboard. Process exited with code %d|@
                """.formatted(process.exitValue()));
            }
        } catch (IOException | InterruptedException e) {
            throw new AstraCliException(IO_ISSUE, """
              @|bold,red Failed to copy token to clipboard: %s|@
            """.formatted(e.getMessage()));
        }
    }

    private static ProcessBuilder copyToClipboardWindows() {
        return new ProcessBuilder("cmd", "/c", "clip");
    }

    private static ProcessBuilder copyToClipboardLinux() {
        if (isCommandAvailable("xclip")) {
            return new ProcessBuilder("xclip", "-selection", "clipboard");
        } else if (isCommandAvailable("xsel")) {
            return new ProcessBuilder("xsel", "--clipboard", "--input");
        } else {
            throw new AstraCliException(PLATFORM_ISSUE, """
              @|bold,red Clipboard copy is not supported on your Linux system.|@

              Please install 'xclip' or 'xsel' to enable clipboard copy functionality.
            """);
        }
    }

    private static ProcessBuilder copyToClipboardMac() {
        return new ProcessBuilder("pbcopy");
    }

    public static boolean isCommandAvailable(String command) {
        try {
            val process = new ProcessBuilder("which", command).start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
