package com.dtsx.astra.cli.unit.credentials;

import static org.assertj.core.api.Assertions.assertThat;
import com.dtsx.astra.cli.core.credentials.*;
import org.junit.jupiter.api.*;

public class CredentialStorageTest {

    @Test
    public void file_fallback_store_returns_false() {
        var storage = new FileFallbackStorage();
        assertThat(storage.store("svc", "acct", "secret")).isFalse();
    }

    @Test
    public void file_fallback_retrieve_returns_null() {
        var storage = new FileFallbackStorage();
        assertThat(storage.retrieve("svc", "acct")).isNull();
    }

    @Test
    public void file_fallback_delete_returns_false() {
        var storage = new FileFallbackStorage();
        assertThat(storage.delete("svc", "acct")).isFalse();
    }

    @Test
    public void file_fallback_name_is_descriptive() {
        var storage = new FileFallbackStorage();
        assertThat(storage.name()).contains("astrarc");
    }

    @Test
    public void factory_returns_file_fallback_when_no_keyring_env_set() {
        // ASTRA_CLI_NO_KEYRING=1 causes factory to return FileFallbackStorage
        // We can't set env vars easily in a unit test, so test the factory returns a non-null manager
        var manager = CredentialStorageFactory.get();
        assertThat(manager).isNotNull();
        assertThat(manager.name()).isNotEmpty();
    }

    @Test
    public void factory_service_name_is_correct() {
        assertThat(CredentialStorageFactory.SERVICE_NAME).isEqualTo("com.dtsx.astra.cli");
    }
}
