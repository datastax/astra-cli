package com.dtsx.astra.cli.gateways.pcu.vendored.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@Jacksonized
@SuperBuilder
public final class PcuGroupCreationRequest extends PcuGroupCreateUpdateRequest {
    private String instanceType;
    private PcuGroupProvisionType provisionType;

    public PcuGroupCreationRequest withDefaultsAndValidations() {
        if (this.provisionType == null) {
            this.provisionType = PcuGroupProvisionType.SHARED;
        }

        if (this.instanceType == null || this.instanceType.isBlank()) {
            this.instanceType = "standard";
        }

        if (this.reserved == null) {
            this.reserved = 0;
        }

        return this;
    }
}
