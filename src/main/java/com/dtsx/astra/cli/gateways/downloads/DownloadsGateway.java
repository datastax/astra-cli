package com.dtsx.astra.cli.gateways.downloads;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.CliProperties.ExternalSoftware;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DownloadsGateway {

    Either<String, List<Path>> downloadCloudSecureBundles(DbRef ref, String dbName, Collection<Datacenter> datacenters);

    Either<String, Path> downloadCqlsh(ExternalSoftware cqlsh);

    Either<String, Path> downloadDsbulk(ExternalSoftware dsbulk);

    Either<String, Path> downloadPulsarShell(ExternalSoftware pulsar);

    Optional<Path> cqlshPath(ExternalSoftware cqlsh);

    Optional<Path> dsbulkPath(ExternalSoftware dsbulk);

    Optional<Path> pulsarShellPath(ExternalSoftware pulsar);
}
