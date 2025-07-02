package com.dtsx.astra.cli.gateways.downloads;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.Token;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.io.File;
import java.util.Collection;
import java.util.List;

public interface DownloadsGateway {
    static DownloadsGateway mkDefault(Token token, AstraEnvironment env) {
        return new DownloadsGatewayImpl(APIProvider.mkDefault(token, env));
    }

    Either<String, List<File>> downloadCloudSecureBundles(DbRef ref, String dbName, Collection<Datacenter> datacenters);

    Either<String, File> downloadCqlshArchive(String url, String tarball);
}
