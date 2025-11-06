package com.dtsx.astra.cli.gateways.downloads;

import com.dtsx.astra.cli.core.properties.CliProperties.ExternalSoftware;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.sdk.db.domain.Datacenter;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DownloadsGateway extends SomeGateway {
    Either<String, List<Path>> downloadCloudSecureBundles(DbRef ref, Collection<Datacenter> datacenters);

    Either<String, Path> downloadCqlsh(ExternalSoftware cqlsh);

    Either<String, Path> downloadDsbulk(ExternalSoftware dsbulk);

    Either<String, Path> downloadPulsarShell(ExternalSoftware pulsar);

    Either<String, Path> downloadAstra(ExternalSoftware astra);

    Optional<Path> cqlshPath(ExternalSoftware cqlsh);

    Optional<Path> dsbulkPath(ExternalSoftware dsbulk);

    Optional<Path> pulsarShellPath(ExternalSoftware pulsar);
}
