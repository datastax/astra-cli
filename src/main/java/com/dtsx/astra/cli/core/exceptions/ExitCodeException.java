package com.dtsx.astra.cli.core.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@RequiredArgsConstructor
public class ExitCodeException extends RuntimeException {
    private final int exitCode;
}
