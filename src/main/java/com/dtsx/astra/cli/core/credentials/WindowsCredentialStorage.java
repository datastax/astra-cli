package com.dtsx.astra.cli.core.credentials;

/**
 * Windows credential storage.
 * Delegates to FileFallbackStorage (~/.astrarc) for initial implementation.
 * Full DPAPI/cmdkey integration is deferred to a future ticket.
 */
public class WindowsCredentialStorage implements CredentialStorageManager {
    private final FileFallbackStorage delegate = new FileFallbackStorage();

    @Override
    public String name() { return delegate.name(); }

    @Override
    public boolean store(String service, String account, String secret) {
        return delegate.store(service, account, secret);
    }

    @Override
    public String retrieve(String service, String account) {
        return delegate.retrieve(service, account);
    }

    @Override
    public boolean delete(String service, String account) {
        return delegate.delete(service, account);
    }
}
