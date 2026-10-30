package com.dtsx.astra.cli.operations;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.AstraConfig.StoredCsToken;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.operations.LoginOperation.OrgInfo;
import com.dtsx.astra.cli.operations.LoginOperation.OrgRoleSelection;
import com.dtsx.astra.cli.operations.LoginOperation.RoleInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * PKCE-04: switch the active AstraCS token to a different org/role combination.
 *
 * <p>Two paths:
 * <ol>
 *   <li><b>Existing token</b> — the user picks a profile from the stored-token list; that
 *       profile's CS token + org/role metadata is copied into the target profile with no
 *       network call.</li>
 *   <li><b>New token</b> — the user picks "[+ Create new token]"; the stored Bearer JWT
 *       from the target profile is used to call GET /v2/oauth/userinfo (org list) and then
 *       POST /v2/oauth/client-token (mint).  If the JWT is absent or the call fails with
 *       401/403, the user is told to run {@code astra login} first — no silent re-login.</li>
 * </ol>
 */
@RequiredArgsConstructor
public class AuthSwitchOperation implements Operation<AuthSwitchOperation.SwitchResult> {

    private static final String DEFAULT_LOGIN_URL = "https://astra.datastax.com/v1/login";

    public record SwitchRequest(
        /** Override the base URL (used in tests; leave empty in production). */
        Optional<String> loginBaseUrl,
        /** Profile to update. Defaults to "default". */
        Optional<ProfileName> profileName,
        /** Pre-select org by name or ID (non-interactive). */
        Optional<String> orgFlag,
        /** Pre-select role by name or ID (non-interactive). */
        Optional<String> roleFlag
    ) {}

    public sealed interface SwitchResult {}
    public record SwitchSuccess(ProfileName profileName, String orgName, String roleName) implements SwitchResult {}
    public record SwitchError(String message) implements SwitchResult {}

    private final CliContext ctx;
    private final SwitchRequest request;

    @Override
    public SwitchResult execute() {
        val targetProfile = request.profileName().orElse(ProfileName.mkUnsafe("default"));
        val config = AstraConfig.readAstraConfigFile(ctx, null, false);

        // If --org/--role provided, skip the stored-token list and go straight to minting.
        if (request.orgFlag().isPresent() || request.roleFlag().isPresent()) {
            return mintNewToken(config, targetProfile);
        }

        // Build the "existing + create new" selection list.
        val stored = config.readStoredCsTokens();
        val CREATE_NEW = "[+ Create new token for this profile]";

        val displayOptions = new ArrayList<String>();
        displayOptions.add(CREATE_NEW);
        stored.forEach(t -> displayOptions.add(t.label()));

        val options = NEList.parse(displayOptions).orElse(null);
        if (options == null) {
            // Shouldn't happen — CREATE_NEW is always present — but guard anyway.
            return mintNewToken(config, targetProfile);
        }

        val chosen = ctx.console().select("Select a token or create a new one:")
            .options(options)
            .requireAnswer()
            .mapper(s -> s)
            .fallbackFlag("--org")
            .fix(ctx.originalArgs(), "--org <org> --role <role>")
            .clearAfterSelection();

        if (chosen == null || chosen.equals(CREATE_NEW)) {
            return mintNewToken(config, targetProfile);
        }

        // Find the matching stored token by label.
        val match = stored.stream().filter(t -> t.label().equals(chosen)).findFirst().orElse(null);
        if (match == null) {
            return new SwitchError("Could not find a stored token matching the selection.");
        }

        return copyExistingToken(config, targetProfile, match);
    }

    // -----------------------------------------------------------------------------------------
    // Copy an existing stored token into the target profile (no network)
    // -----------------------------------------------------------------------------------------

    private SwitchResult copyExistingToken(AstraConfig config, ProfileName targetProfile, StoredCsToken source) {
        val csToken = AstraToken.mkUnsafe(source.csToken());
        config.modify(modCtx -> {
            modCtx.setCsToken(targetProfile, csToken);
            modCtx.setOrgRoleMeta(targetProfile,
                source.orgId(), source.orgName(),
                source.roleId(), source.roleName());
        });
        return new SwitchSuccess(targetProfile, source.orgName(), source.roleName());
    }

    // -----------------------------------------------------------------------------------------
    // Mint a new CS token using the stored Bearer JWT + org/role selection
    // -----------------------------------------------------------------------------------------

    private SwitchResult mintNewToken(AstraConfig config, ProfileName targetProfile) {
        val profile = config.lookupProfile(targetProfile).orElse(null);
        if (profile == null) {
            return new SwitchError("Profile '" + targetProfile.unwrap() + "' does not exist. Run 'astra login' first.");
        }

        val rawToken = profile.token().unsafeUnwrap();
        val sessionBase = sessionBase();

        val orgs = fetchOrgs(sessionBase, rawToken);
        if (orgs == null) {
            return new SwitchError("Failed to fetch org list. Your session may have expired — run 'astra login' to refresh it.");
        }
        if (orgs.isEmpty()) {
            return new SwitchError("No organizations are available for this account.");
        }

        val orgRole = promptOrgRole(orgs);
        if (orgRole == null) {
            return new SwitchError("Org/role selection was cancelled or failed.");
        }

        return doMint(config, targetProfile, sessionBase, rawToken, orgRole);
    }

    private SwitchResult doMint(AstraConfig config, ProfileName targetProfile,
                                String sessionBase, String rawToken, OrgRoleSelection orgRole) {
        try {
            val mapper = new ObjectMapper();
            val bodyJson = mapper.writeValueAsString(
                Map.of("orgId", orgRole.orgId(), "roles", List.of(orgRole.roleId())));

            val httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            val httpReq = HttpRequest.newBuilder()
                .uri(URI.create(sessionBase + "/v2/oauth/client-token"))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + rawToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();
            val httpResp = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofString());

            if (httpResp.statusCode() == 401 || httpResp.statusCode() == 403) {
                return new SwitchError("Your session has expired. Run 'astra login' to get a new token.");
            }
            if (httpResp.statusCode() != 200) {
                return new SwitchError("AstraCS token minting failed (HTTP " + httpResp.statusCode() + ").");
            }

            val csTokenValue = mapper.readTree(httpResp.body()).path("token").asText(null);
            if (csTokenValue == null || csTokenValue.isBlank()) {
                return new SwitchError("AstraCS token minting returned no token field.");
            }

            val csToken = AstraToken.mkUnsafe(csTokenValue);
            config.modify(modCtx -> {
                modCtx.setCsToken(targetProfile, csToken);
                modCtx.setOrgRoleMeta(targetProfile, orgRole.orgId(), orgRole.orgName(), orgRole.roleId(), orgRole.roleName());
            });
            return new SwitchSuccess(targetProfile, orgRole.orgName(), orgRole.roleName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new SwitchError("Token minting was interrupted.");
        } catch (IOException | IllegalArgumentException e) {
            return new SwitchError("Token minting failed: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------------------------
    // Org+role selection (same pattern as LoginOperation)
    // -----------------------------------------------------------------------------------------

    private OrgRoleSelection promptOrgRole(List<OrgInfo> orgs) {
        OrgInfo chosenOrg;
        if (request.orgFlag().isPresent()) {
            val orgFlagVal = request.orgFlag().get();
            chosenOrg = orgs.stream()
                .filter(o -> o.id().equals(orgFlagVal) || o.name().equalsIgnoreCase(orgFlagVal))
                .findFirst().orElse(null);
            if (chosenOrg == null) {
                ctx.console().println("--org '" + orgFlagVal + "' did not match any available org.");
                return null;
            }
        } else if (orgs.size() == 1) {
            chosenOrg = orgs.getFirst();
        } else {
            val orgOptions = NEList.parse(orgs).orElse(null);
            if (orgOptions == null) return null;
            chosenOrg = ctx.console().select("Select an organization:")
                .options(orgOptions)
                .requireAnswer()
                .mapper(OrgInfo::name)
                .fallbackFlag("--org")
                .fix(ctx.originalArgs(), "--org <org>")
                .clearAfterSelection();
        }

        val roles = chosenOrg.roles();
        if (roles.isEmpty()) {
            ctx.console().println("Org '" + chosenOrg.name() + "' has no roles.");
            return null;
        }

        RoleInfo chosenRole;
        if (request.roleFlag().isPresent()) {
            val roleFlagVal = request.roleFlag().get();
            chosenRole = roles.stream()
                .filter(r -> r.id().equals(roleFlagVal) || r.name().equalsIgnoreCase(roleFlagVal))
                .findFirst().orElse(null);
            if (chosenRole == null) {
                ctx.console().println("--role '" + roleFlagVal + "' did not match any available role.");
                return null;
            }
        } else if (roles.size() == 1) {
            chosenRole = roles.getFirst();
        } else {
            val roleOptions = NEList.parse(roles).orElse(null);
            if (roleOptions == null) return null;
            chosenRole = ctx.console().select("Select a role for " + chosenOrg.name() + ":")
                .options(roleOptions)
                .requireAnswer()
                .mapper(RoleInfo::name)
                .fallbackFlag("--role")
                .fix(ctx.originalArgs(), "--role <role>")
                .clearAfterSelection();
        }

        return new OrgRoleSelection(chosenOrg.id(), chosenOrg.name(), chosenRole.id(), chosenRole.name());
    }

    private List<OrgInfo> fetchOrgs(String sessionBase, String rawToken) {
        try {
            val httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            val httpReq = HttpRequest.newBuilder()
                .uri(URI.create(sessionBase + "/v2/oauth/userinfo"))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + rawToken)
                .GET()
                .build();
            val httpResp = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofString());

            if (httpResp.statusCode() == 401 || httpResp.statusCode() == 403) {
                return null; // expired — caller will tell user to re-login
            }
            if (httpResp.statusCode() != 200) {
                ctx.console().println("Warning: could not fetch org list (HTTP " + httpResp.statusCode() + ").");
                return null;
            }

            val mapper = new ObjectMapper();
            val root = mapper.readTree(httpResp.body());
            val orgsNode = root.path("orgs");
            if (!orgsNode.isArray()) return List.of();

            val result = new ArrayList<OrgInfo>();
            for (val orgNode : orgsNode) {
                val orgId = orgNode.path("id").asText("");
                val orgName = orgNode.path("name").asText("");
                val rolesList = new ArrayList<RoleInfo>();
                for (val roleNode : orgNode.path("roles")) {
                    rolesList.add(new RoleInfo(roleNode.path("id").asText(""), roleNode.path("name").asText("")));
                }
                result.add(new OrgInfo(orgId, orgName, List.copyOf(rolesList)));
            }
            return List.copyOf(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (IOException | IllegalArgumentException e) {
            ctx.console().println("Warning: failed to fetch org list: " + e.getMessage());
            return null;
        }
    }

    private String sessionBase() {
        val configured = request.loginBaseUrl()
            .or(() -> Optional.ofNullable(System.getenv("ASTRA_LOGIN_URL")))
            .orElse(DEFAULT_LOGIN_URL);
        return configured.replaceFirst("/v?[0-9]+/.*$", "");
    }
}
