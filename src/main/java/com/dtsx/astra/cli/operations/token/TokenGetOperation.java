package com.dtsx.astra.cli.operations.token;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenGetOperation.TokenGetResponse;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static com.dtsx.astra.cli.core.output.ExitCode.IO_ISSUE;
import static com.dtsx.astra.cli.core.output.ExitCode.PLATFORM_ISSUE;

@RequiredArgsConstructor
public class TokenGetOperation implements Operation<TokenGetResponse> {
    private final CliContext ctx;
    private final Profile profile;
    private final TokenGateway tokenGateway;
    private final TokenGetRequest request;

    public record TokenGetRequest(
        boolean validate,
        boolean copyToClipboard
    ) {}

    public sealed interface TokenGetResponse {}
    public record GotToken(AstraToken token) implements TokenGetResponse {}
    public record CopiedToken() implements TokenGetResponse {}

    @Override
    public TokenGetResponse execute() {
        if (request.validate) {
            tokenGateway.validate(profile.token());
        }

        if (request.copyToClipboard) {
            return copyToClipboard();
        }

        return new GotToken(profile.token());
    }

    private CopiedToken copyToClipboard() {
        return ctx.log().loading("Copying token to clipboard", (_) -> {
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
                    writer.write(profile.token().unsafeUnwrap());
                    writer.flush();
                }

                if (process.waitFor() == 0) {
                    return new CopiedToken();
                } else {
                    throw new AstraCliException(IO_ISSUE, """
                      @|bold,red Failed to copy token to clipboard. Process exited with code %d|@
                    """.formatted(process.exitValue()));
                }
            } catch (IOException | InterruptedException e) {
                throw new AstraCliException(IO_ISSUE, """
                  @|bold,red Failed to copy token to clipboard: %s|@
                """.formatted(e.getMessage()));
            }
        });
    }

    private ProcessBuilder copyToClipboardWindows() {
        return new ProcessBuilder("cmd", "/c", "clip");
    }

    private ProcessBuilder copyToClipboardLinux() {
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

    private ProcessBuilder copyToClipboardMac() {
        return new ProcessBuilder("pbcopy");
    }

    private boolean isCommandAvailable(String command) {
        try {
            val process = new ProcessBuilder("which", command).start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
