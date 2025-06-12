package com.dtsx.astra.cli.core.exceptions;

import com.dtsx.astra.cli.utils.StringUtils;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class AstraCliException extends RuntimeException {
    @Getter
    private final @Nullable Map<String, Object> metadata;

    public AstraCliException(String formatted) {
        this(formatted, null, null);
    }

    public AstraCliException(String formatted, Throwable cause) {
        this(formatted, null, cause);
    }

    public AstraCliException(String formatted, @Nullable Map<String, Object> metadata, @Nullable Throwable cause) {
        super(StringUtils.trimIndent(formatted), cause);
        this.metadata = metadata;
    }

    public boolean shouldDumpLogs() {
        return false;
    }
}
