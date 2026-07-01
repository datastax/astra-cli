package com.dtsx.astra.cli.core.exceptions.internal.db;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;

import static com.dtsx.astra.cli.core.output.ExitCode.DOWNLOAD_ISSUE;

public class ScbDownloadException extends AstraCliException {
    public ScbDownloadException(String reason) {
        super(DOWNLOAD_ISSUE, "@|bold, red Error: Failed to download the secure connect bundle.|@\n\nCause:\n" + reason);
    }
}
