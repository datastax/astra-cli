package com.dtsx.astra.cli.gateways.pcu.vendored.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import lombok.val;

@Jacksonized
@SuperBuilder
public class PcuGroupUpdateRequest extends PcuGroupCreateUpdateRequest {
    // TODO once the bug that causes fields to potentially be lost during partial updates is fixed, we can remove the base parameter here
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
    @Accessors(chain = true)
    public static class InternalRep extends PcuGroupUpdateRequest {
        private String pcuGroupUUID;
        private String instanceType;
        private PcuGroupProvisionType provisionType;

        protected InternalRep(PcuGroupUpdateRequestBuilder<?, ?> b) {
            super(b);
        }
    }
}
