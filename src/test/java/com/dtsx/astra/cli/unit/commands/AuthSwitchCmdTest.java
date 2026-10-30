package com.dtsx.astra.cli.unit.commands;

import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.credentials.CredentialStorageFactory;
import com.dtsx.astra.cli.core.credentials.FileFallbackStorage;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.operations.AuthSwitchOperation;
import com.dtsx.astra.cli.operations.AuthSwitchOperation.SwitchRequest;
import com.dtsx.astra.cli.operations.AuthSwitchOperation.SwitchResult;
import com.dtsx.astra.cli.testlib.Fixtures;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext;
import com.dtsx.astra.cli.testlib.extensions.context.UseTestCtx;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AuthSwitchOperation (PKCE-04).
 *
 * Tests operate directly on AuthSwitchOperation — no picocli wiring needed.
 * A jimfs-backed TestCliContext provides an in-memory config file.
 */
public class AuthSwitchCmdTest {

    @UseTestCtx(fs = "jimfs")
    private TestCliContext ctx;

    // -----------------------------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------------------------

    /** A fake Bearer JWT to store as the profile token. */
    private static final String FAKE_BEARER = Fixtures.Token.unsafeUnwrap();

    /** A fake AstraCS token to store as the CS token in a profile. */
    private static final String FAKE_CS_TOKEN = "AstraCS:cs-mock:existing123";

    /** Userinfo JSON with one org and one role. */
    private static final String USERINFO_RESP = """
        {"orgs":[{"id":"org-uuid-1","name":"Acme Corp","roles":[{"id":"role-uuid-1","name":"Database Administrator"}]}]}
        """;

    /** Userinfo JSON with two orgs, each with one role. */
    private static final String USERINFO_RESP_TWO_ORGS = """
        {"orgs":[
          {"id":"org-uuid-1","name":"Acme Corp","roles":[{"id":"role-uuid-1","name":"Database Administrator"}]},
          {"id":"org-uuid-2","name":"Beta Inc","roles":[{"id":"role-uuid-2","name":"Read Only User"}]}
        ]}
        """;

    /** Minted CS token returned by the mock client-token endpoint. */
    private static final String MINTED_CS_TOKEN = "AstraCS:minted:newtoken456";

    /** Client-token response JSON. */
    private static final String CLIENT_TOKEN_RESP =
        "{\"token\":\"" + MINTED_CS_TOKEN + "\",\"clientId\":\"minted\",\"orgId\":\"org-uuid-1\"}";

    /**
     * Creates an AstraConfig backed by the jimfs scratch file (createIfNotExists=true),
     * then populates a profile with a Bearer JWT and optionally a CS token + org/role meta.
     */
    private AstraConfig setupProfileWithCsToken(String profileName,
                                                String csToken,
                                                String orgId, String orgName,
                                                String roleId, String roleName) {
        val config = AstraConfig.readAstraConfigFile(ctx.get(), null, true);
        val pn = ProfileName.mkUnsafe(profileName);
        config.modify(modCtx -> {
            modCtx.createProfile(pn, AstraToken.mkUnsafe(FAKE_BEARER), AstraEnvironment.PROD);
            modCtx.setCsToken(pn, AstraToken.mkUnsafe(csToken));
            modCtx.setOrgRoleMeta(pn, orgId, orgName, roleId, roleName);
        });
        return AstraConfig.readAstraConfigFile(ctx.get(), null, false);
    }

    /** Creates a profile with a Bearer JWT but no CS token or org/role meta. */
    private AstraConfig setupProfileWithoutCsToken(String profileName) {
        val config = AstraConfig.readAstraConfigFile(ctx.get(), null, true);
        val pn = ProfileName.mkUnsafe(profileName);
        config.modify(modCtx ->
            modCtx.createProfile(pn, AstraToken.mkUnsafe(FAKE_BEARER), AstraEnvironment.PROD)
        );
        return AstraConfig.readAstraConfigFile(ctx.get(), null, false);
    }

    /** Executes the operation with the given request. */
    private SwitchResult runSwitch(SwitchRequest request) {
        return new AuthSwitchOperation(ctx.get(), request).execute();
    }

    // -----------------------------------------------------------------------------------------
    // stored_cs_tokens — readStoredCsTokens round-trip
    // -----------------------------------------------------------------------------------------

    @Nested
    class stored_cs_tokens {

        @Test
        @SneakyThrows
        void returns_profiles_with_full_metadata() {
            // Profile with full meta must appear; profile without meta must be skipped.
            val config = AstraConfig.readAstraConfigFile(ctx.get(), null, true);

            config.modify(modCtx -> {
                // Profile 1: full CS token + meta
                val p1 = ProfileName.mkUnsafe("full-meta");
                modCtx.createProfile(p1, AstraToken.mkUnsafe(FAKE_BEARER), AstraEnvironment.PROD);
                modCtx.setCsToken(p1, AstraToken.mkUnsafe(FAKE_CS_TOKEN));
                modCtx.setOrgRoleMeta(p1, "org-id-1", "Acme Corp", "role-id-1", "Database Administrator");

                // Profile 2: Bearer token only, no CS token or meta
                val p2 = ProfileName.mkUnsafe("no-cs");
                modCtx.createProfile(p2, AstraToken.mkUnsafe(FAKE_BEARER), AstraEnvironment.PROD);
            });

            val fresh = AstraConfig.readAstraConfigFile(ctx.get(), null, false);
            val stored = fresh.readStoredCsTokens();

            assertThat(stored).hasSize(1);
            val t = stored.getFirst();
            assertThat(t.profileName()).isEqualTo(ProfileName.mkUnsafe("full-meta"));
            assertThat(t.csToken()).isEqualTo(FAKE_CS_TOKEN);
            assertThat(t.orgId()).isEqualTo("org-id-1");
            assertThat(t.orgName()).isEqualTo("Acme Corp");
            assertThat(t.roleId()).isEqualTo("role-id-1");
            assertThat(t.roleName()).isEqualTo("Database Administrator");
            assertThat(t.label()).contains("Acme Corp").contains("Database Administrator").contains("full-meta");
        }

        @Test
        @SneakyThrows
        void returns_empty_when_no_profiles_have_metadata() {
            // Only a bare profile — no CS token at all.
            val config = AstraConfig.readAstraConfigFile(ctx.get(), null, true);
            config.modify(modCtx ->
                modCtx.createProfile(
                    ProfileName.mkUnsafe("bare"),
                    AstraToken.mkUnsafe(FAKE_BEARER),
                    AstraEnvironment.PROD
                )
            );

            val fresh = AstraConfig.readAstraConfigFile(ctx.get(), null, false);
            assertThat(fresh.readStoredCsTokens()).isEmpty();
        }
    }

    // -----------------------------------------------------------------------------------------
    // mint_new_token — flags-driven path (--org + --role → mint without interactive prompt)
    // -----------------------------------------------------------------------------------------

    @Nested
    class mint_new_token {

        @BeforeEach
        void useFileStorage() {
            CredentialStorageFactory.setOverrideForTest(new FileFallbackStorage());
        }

        @AfterEach
        void clearStorageOverride() {
            CredentialStorageFactory.clearOverrideForTest();
        }

        @Test
        @SneakyThrows
        void mints_new_cs_token_when_org_and_role_flags_match() {
            setupProfileWithoutCsToken("mint-profile");

            val mockServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            mockServer.createContext("/v2/oauth/userinfo", exchange -> {
                val resp = USERINFO_RESP.strip().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                try (val os = exchange.getResponseBody()) { os.write(resp); }
            });
            mockServer.createContext("/v2/oauth/client-token", exchange -> {
                val resp = CLIENT_TOKEN_RESP.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                try (val os = exchange.getResponseBody()) { os.write(resp); }
            });
            mockServer.start();
            int mockPort = mockServer.getAddress().getPort();

            try {
                val loginBaseUrl = "http://localhost:" + mockPort + "/v1/login";
                val result = runSwitch(new SwitchRequest(
                    Optional.of(loginBaseUrl),
                    Optional.of(ProfileName.mkUnsafe("mint-profile")),
                    Optional.of("Acme Corp"),
                    Optional.of("Database Administrator")
                ));

                assertThat(result).isInstanceOf(AuthSwitchOperation.SwitchSuccess.class);
                val success = (AuthSwitchOperation.SwitchSuccess) result;
                assertThat(success.orgName()).isEqualTo("Acme Corp");
                assertThat(success.roleName()).isEqualTo("Database Administrator");

                // Verify config was updated
                val config = AstraConfig.readAstraConfigFile(ctx.get(), null, false);
                val section = config.lookupSection("mint-profile");
                assertThat(section).isPresent();
                assertThat(section.get().lookupKey("ASTRA_CS_TOKEN"))
                    .isPresent()
                    .hasValue(MINTED_CS_TOKEN);
                assertThat(section.get().lookupKey("ASTRA_CS_ORG_NAME"))
                    .isPresent()
                    .hasValue("Acme Corp");
                assertThat(section.get().lookupKey("ASTRA_CS_ROLE_NAME"))
                    .isPresent()
                    .hasValue("Database Administrator");
            } finally {
                mockServer.stop(0);
            }
        }

        @Test
        @SneakyThrows
        void returns_error_when_userinfo_returns_401() {
            setupProfileWithoutCsToken("expired-profile");

            val mockServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            mockServer.createContext("/v2/oauth/userinfo", exchange -> {
                exchange.sendResponseHeaders(401, 0);
                exchange.getResponseBody().close();
            });
            mockServer.start();
            int mockPort = mockServer.getAddress().getPort();

            try {
                val result = runSwitch(new SwitchRequest(
                    Optional.of("http://localhost:" + mockPort + "/v1/login"),
                    Optional.of(ProfileName.mkUnsafe("expired-profile")),
                    Optional.of("Acme Corp"),
                    Optional.of("Database Administrator")
                ));

                assertThat(result).isInstanceOf(AuthSwitchOperation.SwitchError.class);
                val error = (AuthSwitchOperation.SwitchError) result;
                assertThat(error.message()).containsIgnoringCase("expired")
                    .containsIgnoringCase("login");
            } finally {
                mockServer.stop(0);
            }
        }

        @Test
        @SneakyThrows
        void returns_error_when_profile_does_not_exist() {
            // Ensure config file exists (createIfNotExists=true) but no profile named "ghost"
            AstraConfig.readAstraConfigFile(ctx.get(), null, true);

            val result = runSwitch(new SwitchRequest(
                Optional.of("http://localhost:9/v1/login"), // unreachable — should not be called
                Optional.of(ProfileName.mkUnsafe("ghost-profile")),
                Optional.of("Acme Corp"),
                Optional.of("Database Administrator")
            ));

            assertThat(result).isInstanceOf(AuthSwitchOperation.SwitchError.class);
            val error = (AuthSwitchOperation.SwitchError) result;
            assertThat(error.message()).containsIgnoringCase("ghost-profile")
                .containsIgnoringCase("exist");
        }
    }

    // -----------------------------------------------------------------------------------------
    // copy_existing_token — copy path via stored token
    // -----------------------------------------------------------------------------------------

    @Nested
    class copy_existing_token {

        @BeforeEach
        void useFileStorage() {
            CredentialStorageFactory.setOverrideForTest(new FileFallbackStorage());
        }

        @AfterEach
        void clearStorageOverride() {
            CredentialStorageFactory.clearOverrideForTest();
        }

        @Test
        @SneakyThrows
        void copy_token_and_meta_written_to_config() {
            // Set up a source profile with a fully populated CS token + meta.
            // Then call copyExistingToken indirectly: set up TWO profiles, both with meta,
            // and use --org/--role flags pointing at the org/role in the SECOND profile's stored data.
            // The flags route to mintNewToken — so we can't test copyExistingToken via flags.
            //
            // Instead, verify the copy primitives directly: write a profile with meta, then
            // overwrite the CS token via a second modify, and confirm both keys survive.
            val config = AstraConfig.readAstraConfigFile(ctx.get(), null, true);
            val pn = ProfileName.mkUnsafe("copy-src");
            config.modify(modCtx -> {
                modCtx.createProfile(pn, AstraToken.mkUnsafe(FAKE_BEARER), AstraEnvironment.PROD);
                modCtx.setCsToken(pn, AstraToken.mkUnsafe(FAKE_CS_TOKEN));
                modCtx.setOrgRoleMeta(pn, "org-uuid-1", "Acme Corp", "role-uuid-1", "Database Administrator");
            });

            val fresh = AstraConfig.readAstraConfigFile(ctx.get(), null, false);
            val stored = fresh.readStoredCsTokens();
            assertThat(stored).hasSize(1);

            val src = stored.getFirst();
            assertThat(src.csToken()).isEqualTo(FAKE_CS_TOKEN);
            assertThat(src.orgName()).isEqualTo("Acme Corp");
            assertThat(src.roleName()).isEqualTo("Database Administrator");

            // Now simulate what copyExistingToken does: write the same values into a target profile.
            val target = ProfileName.mkUnsafe("copy-target");
            fresh.modify(modCtx -> {
                modCtx.createProfile(target, AstraToken.mkUnsafe(FAKE_BEARER), AstraEnvironment.PROD);
                modCtx.setCsToken(target, AstraToken.mkUnsafe(src.csToken()));
                modCtx.setOrgRoleMeta(target, src.orgId(), src.orgName(), src.roleId(), src.roleName());
            });

            val afterCopy = AstraConfig.readAstraConfigFile(ctx.get(), null, false);
            val section = afterCopy.lookupSection("copy-target");
            assertThat(section).isPresent();
            assertThat(section.get().lookupKey("ASTRA_CS_TOKEN")).hasValue(FAKE_CS_TOKEN);
            assertThat(section.get().lookupKey("ASTRA_CS_ORG_NAME")).hasValue("Acme Corp");
            assertThat(section.get().lookupKey("ASTRA_CS_ROLE_NAME")).hasValue("Database Administrator");
        }
    }
}
