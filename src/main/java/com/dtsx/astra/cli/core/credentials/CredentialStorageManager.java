package com.dtsx.astra.cli.core.credentials;

/**
 * SPI for secure OS credential storage.
 * Implementations wrap native OS keychain/credential-store tooling.
 */
public interface CredentialStorageManager {
    /** Store a secret. Returns true on success. */
    boolean store(String service, String account, String secret);
    /** Retrieve a secret. Returns null if not found or on error. */
    String retrieve(String service, String account);
    /** Delete a credential entry. Returns true on success. */
    boolean delete(String service, String account);
    /** Human-readable name for logging. */
    String name();
}
