package com.dtsx.astra.cli.core.credentials;

import java.util.List;

public class LinuxSecretServiceStorage implements CredentialStorageManager {
    @Override
    public String name() { return "Linux Secret Service (libsecret)"; }

    @Override
    public boolean store(String service, String account, String secret) {
        try {
            var proc = new ProcessBuilder(List.of("secret-tool", "store",
                    "--label", "Astra CLI",
                    "service", service,
                    "account", account)).start();
            proc.getOutputStream().write((secret + "\n").getBytes());
            proc.getOutputStream().close();
            return proc.waitFor() == 0;
        } catch (Exception e) { return false; }
    }

    @Override
    public String retrieve(String service, String account) {
        try {
            var proc = new ProcessBuilder(List.of("secret-tool", "lookup",
                    "service", service,
                    "account", account)).start();
            String out = new String(proc.getInputStream().readAllBytes()).trim();
            return proc.waitFor() == 0 && !out.isEmpty() ? out : null;
        } catch (Exception e) { return null; }
    }

    @Override
    public boolean delete(String service, String account) {
        try {
            return new ProcessBuilder(List.of("secret-tool", "clear",
                    "service", service,
                    "account", account))
                    .redirectErrorStream(true).start().waitFor() == 0;
        } catch (Exception e) { return false; }
    }
}
