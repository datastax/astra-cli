package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.sdk.db.domain.CloudProviderType;

public enum CloudProvider {
    GCP,
    AZURE,
    AWS;

    public CloudProviderType toSdkType() {
        return CloudProviderType.valueOf(this.name());
    }

    public static CloudProvider fromSdkType(CloudProviderType cpType) {
        return CloudProvider.fromString(cpType.name());
    }

    public static CloudProvider fromString(String cpStr) {
        return CloudProvider.valueOf(cpStr.toUpperCase());
    }
}
