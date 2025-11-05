package com.dtsx.astra.cli.gateways.pcu.vendored.domain;

import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PcuGroup {
    @Setter(onMethod = @__(@JsonSetter("uuid")))
    @Getter(onMethod = @__(@JsonGetter("uuid")))
    private String id;
    private String orgId;

    private String title;
    private String description;

    private CloudProviderType cloudProvider;
    private String region;

    private String instanceType;
    private PcuGroupProvisionType provisionType;

    private int min;
    private int max;
    private int reserved;

    private String createdAt;
    private String updatedAt;
    private String createdBy;
    private String updatedBy;

    private PcuGroupStatusType status;
}
