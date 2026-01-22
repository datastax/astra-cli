package com.dtsx.astra.cli.gateways.pcu.vendored.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = false)
public enum PcuGroupProvisionType {
    SHARED("shared"),
    DEDICATED("dedicated");

    @JsonValue
    private final String fieldValue;
}
