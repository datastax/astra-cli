package com.dtsx.astra.cli.gateways.pcu.vendored.domain;

import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
public sealed abstract class PcuGroupCreateUpdateRequest permits PcuGroupCreationRequest, PcuGroupUpdateRequest {
    protected String title;
    protected String description;

    protected CloudProviderType cloudProvider;
    protected String region;

    protected Integer min; // Integers so they're nullable
    protected Integer max;
    protected Integer reserved;

    protected void validate() {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("PCU group title is required");
        }

        if (cloudProvider == null) {
            throw new IllegalArgumentException("PCU group cloud provider is required");
        }

        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("PCU group region is required");
        }

        if (min == null || min < 1) {
            throw new IllegalArgumentException("PCU group min must be >= 1");
        }

        if (max == null || max < min) {
            throw new IllegalArgumentException("PCU group max must be >= min");
        }

        if (reserved != null && (reserved < 0 || reserved > min)) {
            throw new IllegalArgumentException("PCU group reserved must be non-negative and <= min");
        }
    }
}
