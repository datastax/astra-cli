package com.dtsx.astra.cli.gateways.pcu.vendored.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

// TODO add the rest of the fields once the PCU team is clear about what is going on
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PcuGroupDatacenterAssociation {
    private String pcuGroupUUID;
    private String datacenterUUID;
}
