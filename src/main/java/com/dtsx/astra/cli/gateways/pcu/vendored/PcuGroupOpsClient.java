package com.dtsx.astra.cli.gateways.pcu.vendored;

import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroup;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupStatusType;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupUpdateRequest;
import com.dtsx.astra.cli.gateways.pcu.vendored.exception.PcuGroupNotFoundException;
import com.dtsx.astra.sdk.AbstractApiClient;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.JsonUtils;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.Optional;

@Slf4j
@Accessors(fluent = false)
public class PcuGroupOpsClient extends AbstractApiClient {
    @Getter
    private final String pcuGroupId;

    public PcuGroupOpsClient(String token, String pcuGroupId) {
        this(token, AstraEnvironment.PROD, pcuGroupId);
    }

    public PcuGroupOpsClient(String token, AstraEnvironment env, String pcuGroupId) {
        super(token, env);
        this.pcuGroupId = pcuGroupId;
    }

    @Override
    public String getServiceName() {
        return "pcu.group";
    }

    // ---------------------------------
    // ----       READ              ----
    // ---------------------------------

    public Optional<PcuGroup> find() {
        try {
            return Optional.of(get());
        } catch (PcuGroupNotFoundException e) {
            return Optional.empty();
        }
    }

    public PcuGroup get() {
        return new PcuGroupsClient(token, environment).findById(pcuGroupId).orElseThrow(() -> PcuGroupNotFoundException.forId(pcuGroupId));
    }

    public boolean exist() {
        return find().isPresent();
    }

    public boolean isActive() {
        return PcuGroupStatusType.ACTIVE == get().getStatus();
    }

    public boolean isCreatedOrActive() {
        return PcuGroupStatusType.CREATED == get().getStatus() || isActive();
    }

    // ---------------------------------
    // ----       UPDATE            ----
    // ---------------------------------

    public void update(PcuGroupUpdateRequest req) {
        val base = get();
        PUT(getEndpointPcus(), JsonUtils.marshall(List.of(req.withDefaultsAndValidations(base))), getOperationName("update"));
    }

    // ---------------------------------
    // ----       MAINTENANCE       ----
    // ---------------------------------

    public void park() {
        val res = POST(getEndpointPcus() + "/park/" + pcuGroupId, getOperationName("park"));

        if (res.getCode() >= 300) {
            throw new IllegalStateException("Expected code 200 to park pcu group but got " + res.getCode() + "body=" + res.getBody());
        }
    }

    public void unpark() {
        val res = POST(getEndpointPcus() + "/unpark/" + pcuGroupId, getOperationName("unpark"));

        if (res.getCode() >= 300) {
            throw new IllegalStateException("Expected code 200 to unpark pcu group but got " + res.getCode() + "body=" + res.getBody());
        }
    }

    public void delete() {
        if (!exist()) {
            throw PcuGroupNotFoundException.forId(pcuGroupId);
        }
        DELETE(getEndpointPcus() + "/" + pcuGroupId, getOperationName("delete"));
    }

    // ---------------------------------
    // ----       Utilities         ----
    // ---------------------------------

    public PcuGroupDatacenterAssociationsClient datacenterAssociations() {
        return new PcuGroupDatacenterAssociationsClient(token, environment, pcuGroupId);
    }

    public String getEndpointPcus() {
        return ApiLocator.getApiDevopsEndpoint(environment) + "/pcus";
    }
}
