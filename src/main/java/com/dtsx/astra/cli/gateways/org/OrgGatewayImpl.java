package com.dtsx.astra.cli.gateways.org;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.exception.AuthenticationException;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.graalvm.collections.Pair;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class OrgGatewayImpl implements OrgGateway {
    private final CliContext ctx;
    private final APIProvider apiProvider;

    @Override
    public Organization current() {
        return ctx.log().loading("Fetching details about the current organization", (_) -> {
            return apiProvider.astraOpsClient().getOrganization();
        });
    }

    @RequiredArgsConstructor
    public static class StatelessImpl implements OrgGateway.Stateless {
        private final CliContext ctx;

        @Override
        public Optional<Pair<AstraEnvironment, Organization>> resolveOrganizationEnvironment(AstraToken token) {
            val baseMsg = "Resolving the Astra environment for the given token";

            return ctx.log().loading(baseMsg, (updateMsg) -> {
                val environments = List.of(
                    AstraEnvironment.PROD,
                    AstraEnvironment.DEV,
                    AstraEnvironment.TEST
                );

                for (val env : environments) {
                    try {
                        updateMsg.accept(baseMsg + " (trying @!" + env.name().toLowerCase() + "!@)");

                        var apiProvider = APIProvider.mkDefault(token, env, ctx);
                        var org = apiProvider.astraOpsClient().getOrganization();
                        return Optional.of(Pair.create(env, org));
                    } catch (AuthenticationException _) {
                        // whatever
                    } catch (Exception e) {
                        ctx.log().exception(e);
                    }
                }

                return Optional.empty();
            });
        }
    }
}
