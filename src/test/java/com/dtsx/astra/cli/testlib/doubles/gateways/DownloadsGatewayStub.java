package com.dtsx.astra.cli.testlib.doubles.gateways;

import com.dtsx.astra.cli.core.CliProperties.ExternalSoftware;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.sdk.db.domain.Datacenter;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.testlib.doubles.Utils.methodIllegallyCalled;

public class DownloadsGatewayStub implements DownloadsGateway {
    @Override
    public Either<String, List<Path>> downloadCloudSecureBundles(DbRef ref, String dbName, Collection<Datacenter> datacenters) {
        return methodIllegallyCalled();
    }

    @Override
    public Either<String, Path> downloadCqlsh(ExternalSoftware cqlsh) {
        return methodIllegallyCalled();
    }

    @Override
    public Either<String, Path> downloadDsbulk(ExternalSoftware dsbulk) {
        return methodIllegallyCalled();
    }

    @Override
    public Either<String, Path> downloadPulsarShell(ExternalSoftware pulsar) {
        return methodIllegallyCalled();
    }

    @Override
    public Optional<Path> cqlshPath(ExternalSoftware cqlsh) {
        return methodIllegallyCalled();
    }

    @Override
    public Optional<Path> dsbulkPath(ExternalSoftware dsbulk) {
        return methodIllegallyCalled();
    }

    @Override
    public Optional<Path> pulsarShellPath(ExternalSoftware pulsar) {
        return methodIllegallyCalled();
    }
}
