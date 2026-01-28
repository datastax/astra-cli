package com.dtsx.astra.cli.gateways.pcu.vendored.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(fluent = false)
public class PcuGroupDatacenterAssociation {
    private String pcuGroupUUID;
    private String datacenterUUID;
    private String clusterName;
    private String clusterUUID;
}
