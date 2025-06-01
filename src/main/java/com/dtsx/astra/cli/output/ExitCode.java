package com.dtsx.astra.cli.output;

import lombok.Getter;

@Getter
public enum ExitCode {
    SUCCESS(0),
    UNAVAILABLE(2),
    INVALID_PARAMETER(4),
    NOT_FOUND(5),
    ALREADY_EXIST(7),
    CANNOT_CONNECT(8),
    CONFIGURATION(9),
    INVALID_ARGUMENT(11),
    INVALID_OPTION(12),
    INVALID_OPTION_VALUE(13),
    UNRECOGNIZED_COMMAND(14),
    CONFLICT(15),
    INTERNAL_ERROR(100);

    private final int code;

    ExitCode(int code) {
        this.code = code;
    }
}
