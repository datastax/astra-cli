package com.dtsx.astra.cli.core.exceptions;

import com.dtsx.astra.cli.utils.StringUtils;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class AstraCliException extends RuntimeException {
    @Getter
    private final @Nullable Map<String, Object> metadata;

    @Getter
    private final CliExceptionCode code;

    public AstraCliException(String formatted) {
        this(null, formatted, null);
    }

    public AstraCliException(CliExceptionCode code, String formatted) {
        this(code, formatted, null);
    }

    public AstraCliException(CliExceptionCode code, String formatted, @Nullable Map<String, Object> metadata) {
        super(StringUtils.trimIndent(formatted));
        this.metadata = metadata;
        this.code = code;
    }

    public boolean shouldDumpLogs() {
        return false;
    }

    public boolean shouldPrintHelpMessage() {
        return false;
    }
}
