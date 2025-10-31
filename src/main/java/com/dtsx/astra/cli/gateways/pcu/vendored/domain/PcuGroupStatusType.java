package com.dtsx.astra.cli.gateways.pcu.vendored.domain;

public enum PcuGroupStatusType {
    CREATED,
    PLACING,
    INITIALIZING,
    ACTIVE,
    PARKED,
    PARKING,
    UNPARKING,
    OTHER // TODO make this work with Jackson
}
