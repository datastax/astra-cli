package com.dtsx.astra.cli.core.output;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Value;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Value
@JsonSerialize
public class JsonResponse {
    int code;
    @Nullable Object data;
    @Nullable String message;

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
