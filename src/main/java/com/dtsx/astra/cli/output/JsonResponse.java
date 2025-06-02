package com.dtsx.astra.cli.output;

import com.dtsx.astra.cli.exceptions.AstraCliException;
import lombok.val;
import org.jetbrains.annotations.Nullable;

public record JsonResponse(int code, @Nullable Object data, @Nullable String message) {
    public static JsonResponse ok(Object data) {
        return new JsonResponse(0, data, null);
    }

    public static JsonResponse error(int code, Throwable error) {
        val metadata = (error instanceof AstraCliException cliErr)
            ? cliErr.getMetadata()
            : null;

        return new JsonResponse(code, metadata, error.getMessage());
    }
}
