package com.dtsx.astra.cli.gateways.upgrade;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.Version;
import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.utils.HttpUtils;
import com.dtsx.astra.cli.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class UpgradeGatewayImpl implements UpgradeGateway {
    private final CliContext ctx;

    @Override
    public Version latestVersion(boolean includePreReleases) {
        return ctx.log().loading("Resolving the version to download", (_) -> (
            (includePreReleases)
                ? fetchLatestIncPreRelease()
                : fetchLatestFullRelease()
        ));
    }

    private Version fetchLatestFullRelease() {
        return ctx.log().loading("Resolving latest full release of @!astra!@", (_) -> {
            val endpoint = ctx.properties().cliGithubApiReposUrl() + "/releases/latest";

            val response = HttpUtils.GET(endpoint, c -> c, r -> r);

            if (response.statusCode() == 404) {
                throw new AstraCliException(ExitCode.RELEASE_NOT_FOUND, """
                  @|bold,red Error: Cannot find latest release from @|underline %s|@|@
                """.formatted(endpoint));
            }

            if (response.statusCode() >= 400) {
                throw new AstraCliException(ExitCode.RELEASE_NOT_FOUND, """
                  @|bold,red An error occurred while fetching the latest release from %s|@
                
                  Status:
                  @!%d!@
                
                  Body:
                  %s
                """.formatted(endpoint, response.statusCode(), response.body()));
            }

            val json = JsonUtils.readTree(response.body());

            System.out.println(json.get("tag_name").asText());

            return Version.mkUnsafe(json.get("tag_name").asText());
        });
    }

    private Version fetchLatestIncPreRelease() {
        return ctx.log().loading("Resolving latest release of @!astra!@", (updateMsg) -> {
            var attempt = 1;

            while (true) {
                val endpoint = ctx.properties().cliGithubApiReposUrl() + "/releases?per_page=1&page=" + attempt;

                val response = HttpUtils.GET(endpoint, c -> c, r -> r);

                if (response.statusCode() >= 400 && response.statusCode() != 404) {
                    throw new AstraCliException(ExitCode.RELEASE_NOT_FOUND, """
                      @|bold,red An error occurred while fetching the latest release from %s|@

                      Status:
                      @!%d!@

                      Body:
                      %s
                    """.formatted(endpoint, response.statusCode(), response.body()));
                }

                val json = JsonUtils.readTree(response.body());

                if (response.statusCode() == 404 || json.isEmpty()) {
                    throw new AstraCliException(ExitCode.RELEASE_NOT_FOUND, """
                      @|bold,red Error: Cannot find latest release from @|underline %s|@|@
                    """.formatted(endpoint));
                }

                // very, very unlikely to happen
                if (json.isArray() && json.get(0).get("draft").asBoolean()) {
                    updateMsg.accept("Resolving latest release of @!astra!@ (attempt %d)".formatted(++attempt));
                    continue;
                }

                return Version.mkUnsafe(json.get(0).get("tag_name").asText());
            }
        });
    }
}
