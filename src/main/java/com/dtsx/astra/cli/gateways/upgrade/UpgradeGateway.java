package com.dtsx.astra.cli.gateways.upgrade;

import com.dtsx.astra.cli.core.models.Version;

public interface UpgradeGateway {
    Version latestVersion(boolean includePreReleases);
}
