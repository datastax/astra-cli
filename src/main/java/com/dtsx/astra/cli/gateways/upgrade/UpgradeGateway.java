package com.dtsx.astra.cli.gateways.upgrade;

import com.dtsx.astra.cli.core.models.Version;
import com.dtsx.astra.cli.gateways.SomeGateway;

public interface UpgradeGateway extends SomeGateway {
    Version latestVersion(boolean includePreReleases);
}
