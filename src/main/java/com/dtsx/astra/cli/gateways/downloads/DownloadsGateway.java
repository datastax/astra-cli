package com.dtsx.astra.cli.gateways.downloads;

import com.dtsx.astra.cli.CLIProperties.ExternalSoftware;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DownloadsGateway {
    static DownloadsGateway mkDefault(AstraToken token, AstraEnvironment env) {
        return new DownloadsGatewayImpl(APIProvider.mkDefault(token, env));
    }

    Either<String, List<File>> downloadCloudSecureBundles(DbRef ref, String dbName, Collection<Datacenter> datacenters);

    Either<String, File> downloadCqlsh(ExternalSoftware cqlsh);

    Either<String, File> downloadDsbulk(ExternalSoftware dsbulk);

    Either<String, File> downloadPulsarShell(ExternalSoftware pulsar);

    Optional<File> cqlshPath(ExternalSoftware cqlsh);

    Optional<File> dsbulkPath(ExternalSoftware dsbulk);

    Optional<File> pulsarShellPath(ExternalSoftware pulsar);
}
