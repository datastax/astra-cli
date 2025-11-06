package com.dtsx.astra.cli.gateways.pcu.vendored;

import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroup;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupCreationRequest;
import com.dtsx.astra.cli.gateways.pcu.vendored.exception.PcuGroupNotFoundException;
import com.dtsx.astra.cli.gateways.pcu.vendored.exception.PcuGroupsNotFoundException;
import com.dtsx.astra.sdk.AbstractApiClient;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.ApiResponseError;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public class PcuGroupsClient extends AbstractApiClient {
    private static final TypeReference<List<PcuGroup>> RESPONSE_PCU_GROUPS =
        new TypeReference<>(){};

    public PcuGroupsClient(String token) {
        super(token, AstraEnvironment.PROD);
    }

    public PcuGroupsClient(String token, AstraEnvironment env) {
        super(token, env);
    }

    @Override
    public String getServiceName() {
        return "pcu.groups";
    }

    // ---------------------------------
    // ----        CRUD             ----
    // ---------------------------------

    public PcuGroup create(PcuGroupCreationRequest req) {
        val res = POST(getEndpointPcus(), JsonUtils.marshall(List.of(req.withDefaultsAndValidations())), getOperationName("create"));

        if (HttpURLConnection.HTTP_CREATED != res.getCode()) {
            throw new IllegalStateException("Expected code 201 to create pcu group but got " + res.getCode() + "body=" + res.getBody());
        }

        return JsonUtils.unmarshallType(res.getBody(), RESPONSE_PCU_GROUPS).getFirst();
    }

    public Optional<PcuGroup> findById(String id) {
        try {
            return findAllImpl(List.of(id), "id", (_e) -> PcuGroupNotFoundException.forId(id)).findFirst();
        } catch (PcuGroupNotFoundException e) {
            return Optional.empty();
        }
    }

    public Stream<PcuGroup> findByTitle(String title) {
        return findAll().filter(pg -> title.equals(pg.getTitle())); // order is important here since pg.title is nullable
    }

    public Optional<PcuGroup> findFirstByTitle(String title) {
        return findByTitle(title).findFirst();
    }

    public Stream<PcuGroup> findAll() {
        return findAll(null);
    }

    public Stream<PcuGroup> findAll(List<String> ids) {
        return findAllImpl(ids, "ids[%d]", (e) -> new PcuGroupsNotFoundException(e.getErrors().getFirst().getMessage()));
    }

    protected interface FindAll404Handler {
        RuntimeException getError(ApiResponseError res);
    }

    private record FindAllReqBody(List<String> pcuGroupUUIDs) {}

    protected Stream<PcuGroup> findAllImpl(List<String> ids, String validationErrorFmtStr, FindAll404Handler on404) {
        if (ids != null) {
            if (ids.isEmpty()) {
                return Stream.of();
            }

            for (var i = 0; i < ids.size(); i++) {
//                Assert.isUUID(ids.get(i), validationErrorFmtStr.formatted(i));
            }
        }

        val reqBody = JsonUtils.marshall(new FindAllReqBody(ids));
        val res = POST(getEndpointPcus() + "/actions/get", reqBody, getOperationName("find"));

        try {
            return JsonUtils.unmarshallType(res.getBody(), RESPONSE_PCU_GROUPS).stream();
        } catch(Exception e) {
            ApiResponseError responseError = null;

            try {
                responseError = JsonUtils.unmarshallBean(res.getBody(), ApiResponseError.class);
            } catch (Exception ignored) {}


            if (responseError != null && res.getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                throw on404.getError(responseError);
            }

            if (responseError != null && responseError.getErrors() != null && !responseError.getErrors().isEmpty()) {
                if (responseError.getErrors().getFirst().getId() == 340018) {
                    throw new IllegalArgumentException("You have provided an invalid token, please check", e);
                }
            }

            throw e;
        }
    }

    // ---------------------------------
    // ----       Utilities         ----
    // ---------------------------------

    public PcuGroupOpsClient group(String pcuGroupId) {
        return new PcuGroupOpsClient(getToken(), getEnvironment(), pcuGroupId);
    }

    public String getEndpointPcus() {
        return ApiLocator.getApiDevopsEndpoint(environment) + "/pcus";
    }
}
