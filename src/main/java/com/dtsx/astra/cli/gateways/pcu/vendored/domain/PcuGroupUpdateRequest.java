package com.dtsx.astra.cli.gateways.pcu.vendored.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import lombok.val;

@Jacksonized
@SuperBuilder
@Accessors(fluent = false)
public class PcuGroupUpdateRequest extends PcuGroupCreateUpdateRequest {
    public PcuGroupCreateUpdateRequest withDefaultsAndValidations(PcuGroup base) {
        val internalRep = new InternalRep(
            builder()
                .title(this.title == null ? base.getTitle() : this.title)
                .description(this.description == null ? base.getDescription() : this.description)
                .cloudProvider(this.cloudProvider == null ? base.getCloudProvider() : this.cloudProvider)
                .region(this.region == null ? base.getRegion() : this.region)
                .min(this.min == null ? base.getMin() : this.min)
                .max(this.max == null ? base.getMax() : this.max)
                .reserved(this.reserved == null ? base.getReserved() : this.reserved)
        );

        internalRep.validate();

        return internalRep
            .setPcuGroupUUID(base.getId())
            .setInstanceType(base.getInstanceType())
            .setProvisionType(base.getProvisionType());
    }

    @Setter
    @Getter
    @Accessors(chain = true, fluent = false)
    public static class InternalRep extends PcuGroupUpdateRequest {
        private String pcuGroupUUID;
        private String instanceType;
        private PcuGroupProvisionType provisionType;

        protected InternalRep(PcuGroupUpdateRequestBuilder<?, ?> b) {
            super(b);
        }
    }
}
