package com.dtsx.astra.cli.gateways.pcu.vendored.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PcuGroupDatacenterAssociation {
    private String pcuGroupUUID;
    private String datacenterUUID;
    private String clusterName;
    private String clusterUUID;
}
