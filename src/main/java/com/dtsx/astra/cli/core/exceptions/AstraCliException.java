package com.dtsx.astra.cli.core.exceptions;

import com.dtsx.astra.cli.core.output.output.Hint;
import com.dtsx.astra.cli.utils.StringUtils;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Getter
public class AstraCliException extends RuntimeException {
    private final @Nullable Map<String, Object> metadata;
    private final @Nullable List<Hint> nextSteps;
    private final CliExceptionCode code;

    public AstraCliException(String formatted) {
        this(null, formatted, null, null);
    }

    public AstraCliException(CliExceptionCode code, String formatted) {
        this(code, formatted, null, null);
    }

    public AstraCliException(CliExceptionCode code, String formatted, @Nullable Map<String, Object> metadata) {
        this(code, formatted, metadata, null);
    }

    public AstraCliException(CliExceptionCode code, String formatted, @Nullable List<Hint> nextSteps) {
        this(code, formatted, null, nextSteps);
    }

    public AstraCliException(CliExceptionCode code, String formatted, @Nullable Map<String, Object> metadata, @Nullable List<Hint> nextSteps) {
        super(StringUtils.trimIndent(formatted));
        this.nextSteps = nextSteps;
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
