package com.dtsx.astra.cli.core.credentials;

/**
 * Headless/CI fallback: signals that the credential should be stored in ~/.astrarc.
 * The caller (AstraConfig) handles the actual file write.
 * Used when: ASTRA_CLI_NO_KEYRING=1, no keychain daemon detected, or Windows.
 */
public class FileFallbackStorage implements CredentialStorageManager {
    @Override public String name() { return "File (~/.astrarc, chmod 600)"; }
    @Override public boolean store(String service, String account, String secret) { return false; }
    @Override public String retrieve(String service, String account) { return null; }
    @Override public boolean delete(String service, String account) { return false; }
}
