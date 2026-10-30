package com.dtsx.astra.cli.core.credentials;

import java.util.List;

public class MacKeychainStorage implements CredentialStorageManager {
    @Override
    public String name() { return "macOS Keychain"; }

    @Override
    public boolean store(String service, String account, String secret) {
        return run(List.of("/usr/bin/security", "add-generic-password",
                "-s", service, "-a", account, "-w", secret, "-U")) == 0;
    }

    @Override
    public String retrieve(String service, String account) {
        var result = runCapture(List.of("/usr/bin/security", "find-generic-password",
                "-s", service, "-a", account, "-w"));
        return result == null ? null : result.trim().isEmpty() ? null : result.trim();
    }

    @Override
    public boolean delete(String service, String account) {
        return run(List.of("/usr/bin/security", "delete-generic-password",
                "-s", service, "-a", account)) == 0;
    }

    private int run(List<String> cmd) {
        try {
            return new ProcessBuilder(cmd).redirectErrorStream(true).start().waitFor();
        } catch (Exception e) { return -1; }
    }

    private String runCapture(List<String> cmd) {
        try {
            var proc = new ProcessBuilder(cmd).start();
            String out = new String(proc.getInputStream().readAllBytes()).trim();
            return proc.waitFor() == 0 ? out : null;
        } catch (Exception e) { return null; }
    }
}
