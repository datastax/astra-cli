package com.dtsx.astra.cli.core.exceptions;

import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.utils.StringUtils;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.SequencedMap;

@Getter
public class AstraCliException extends RuntimeException {
    private final @Nullable SequencedMap<String, Object> metadata;
    private final @Nullable List<Hint> nextSteps;
    private final ExitCode code;

    public AstraCliException(String message) {
        this(null, message, null, null);
    }

    public AstraCliException(ExitCode code, String message) {
        this(code, message, null, null);
    }

    public AstraCliException(ExitCode code, String message, @Nullable SequencedMap<String, Object> metadata) {
        this(code, message, metadata, null);
    }

    public AstraCliException(ExitCode code, String message, @Nullable List<Hint> nextSteps) {
        this(code, message, null, nextSteps);
    }

    public AstraCliException(ExitCode code, String message, @Nullable SequencedMap<String, Object> metadata, @Nullable List<Hint> nextSteps) {
        super(AstraConsole.format(StringUtils.trimIndent(message)));
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
