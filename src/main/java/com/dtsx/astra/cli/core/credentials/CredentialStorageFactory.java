package com.dtsx.astra.cli.core.credentials;

public class CredentialStorageFactory {
    public static final String SERVICE_NAME = "com.dtsx.astra.cli";
    public static final String NO_KEYRING_ENV = "ASTRA_CLI_NO_KEYRING";
    public static final String STORAGE_KEY_SUFFIX = "ASTRA_CS_STORAGE";

    /** Test-only override. Null means auto-detect from OS. */
    private static volatile CredentialStorageManager testOverride = null;

    /** For test use only: override the detected storage manager. */
    public static void setOverrideForTest(CredentialStorageManager override) {
        testOverride = override;
    }

    /** For test use only: clear any override set by {@link #setOverrideForTest}. */
    public static void clearOverrideForTest() {
        testOverride = null;
    }

    public static CredentialStorageManager get() {
        CredentialStorageManager override = testOverride;
        if (override != null) {
            return override;
        }
        if ("1".equals(System.getenv(NO_KEYRING_ENV))) {
            return new FileFallbackStorage();
        }
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac")) {
            return new MacKeychainStorage();
        }
        if (os.contains("linux")) {
            return new LinuxSecretServiceStorage();
        }
        // Windows or unknown: file fallback
        return new FileFallbackStorage();
    }
}
