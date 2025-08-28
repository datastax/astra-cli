package com.dtsx.astra.cli.commands.streaming;

import com.dtsx.astra.cli.core.CliConstants.$Cloud;
import com.dtsx.astra.cli.core.CliConstants.$Regions;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.core.models.TenantStatus;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.StreamingCreateOperation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.val;
import org.graalvm.collections.Pair;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.TENANT_ALREADY_EXISTS;
import static com.dtsx.astra.cli.operations.streaming.StreamingCreateOperation.*;
import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;

@Command(
    name = "create",
    description = "Create a new streaming tenant"
)
@Example(
    comment = "Create a basic streaming tenant in the 'us-east1' region",
    command = "${cli.name} streaming create my_tenant --region us-east1"
)
@Example(
    comment = "Create a tenant with a specific cloud provider",
    command = "${cli.name} streaming create my_tenant --region us-east1 --cloud AWS"
)
@Example(
    comment = "Create a tenant with a dedicated cluster",
    command = "${cli.name} streaming create my_tenant --cluster my_cluster"
)
@Example(
    comment = "Create a tenant if it doesn't already exist",
    command = "${cli.name} streaming create my_tenant --region us-east1 --if-not-exists"
)
public class StreamingCreateCmd extends AbstractStreamingTenantSpecificCmd<StreamingCreateResult> {
    @Option(
        names = { "--if-not-exists" },
        description = { "Don't error if the tenant already exists", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean $ifNotExists;

    @ArgGroup(heading = "%nTenant configuration options:%n", multiplicity = "1")
    public TenantCreationOptions $tenantCreationOptions;

    public static class TenantCreationOptions {
        @Option(
            names = { "--plan" },
            paramLabel = "PLAN",
            description = { "Plan for the tenant", DEFAULT_VALUE },
            defaultValue = "serverless"
        )
        public String $plan;

        @Option(
            names = { "-e", "--email" },
            paramLabel = "EMAIL",
            description = "User email"
        )
        public String $userEmail;

        @ArgGroup(multiplicity = "1")
        public ClusterOrCloud $clusterOrCloud;
    }

    public static class ClusterOrCloud {
        @Option(
            names = { "--cluster" },
            paramLabel = "CLUSTER",
            description = "Dedicated cluster, replacement for cloud/region"
        )
        public Optional<String> $cluster;

        @ArgGroup(exclusive = false)
        public @Nullable RegionSpec $regionSpec;
    }

    public static class RegionSpec {
        @Option(
            names = { $Regions.LONG, $Regions.SHORT },
            description = "Cloud provider region to provision",
            paramLabel = $Regions.LABEL,
            required = true
        )
        public RegionName $region;

        @Option(
            names = { $Cloud.LONG, $Cloud.SHORT },
            description = "The cloud provider where the tenant should be created. Inferred from the region if not provided.",
            paramLabel = $Cloud.LABEL
        )
        public Optional<CloudProviderType> $cloud;
    }

    @Override
    protected Operation<StreamingCreateResult> mkOperation() {
        return new StreamingCreateOperation(streamingGateway, new StreamingCreateRequest(
            $tenantName,
            ($tenantCreationOptions.$clusterOrCloud.$regionSpec != null)
                ? Either.right(Pair.create($tenantCreationOptions.$clusterOrCloud.$regionSpec.$cloud, $tenantCreationOptions.$clusterOrCloud.$regionSpec.$region))
                : Either.left($tenantCreationOptions.$clusterOrCloud.$cluster.orElseThrow()),
            $tenantCreationOptions.$plan,
            $tenantCreationOptions.$userEmail,
            $ifNotExists
        ));
    }

    @Override
    protected final OutputAll execute(Supplier<StreamingCreateResult> result) {
        return switch (result.get()) {
            case TenantAlreadyExistsWithStatus(var tenantName, var currStatus) -> handleTenantAlreadyExistsWithStatus(tenantName, currStatus);
            case TenantAlreadyExistsIllegallyWithStatus(var tenantName, var currStatus) -> throwTenantAlreadyExistsWithStatus(tenantName, currStatus);
            case TenantCreated(var tenantName, var currStatus) -> handleTenantCreated(tenantName, currStatus);
        };
    }

    private OutputAll handleTenantAlreadyExistsWithStatus(TenantName tenantName, TenantStatus currStatus) {
        val message = "Tenant %s already exists and has status %s.".formatted(
            ctx.highlight(tenantName),
            ctx.highlight(currStatus)
        );

        val data = mkData(tenantName, false, currStatus);

        return OutputAll.response(message, data, List.of(
            new Hint("Get information about the existing tenant:", "${cli.name} streaming get %s".formatted(tenantName))
        ));
    }

    private <T> T throwTenantAlreadyExistsWithStatus(TenantName tenantName, TenantStatus currStatus) {
        throw new AstraCliException(TENANT_ALREADY_EXISTS, """
          @|bold,red Error: Tenant %s already exists and has status %s.|@
        
          To ignore this error, provide the @!--if-not-exists!@ flag to skip this error if the tenant already exists.
        """.formatted(
            tenantName,
            currStatus
        ), List.of(
            new Hint("Example fix:", originalArgs(), "--if-not-exists"),
            new Hint("Get information about the existing tenant:", "${cli.name} streaming get %s".formatted(tenantName))
        ));
    }

    private OutputAll handleTenantCreated(TenantName tenantName, TenantStatus currStatus) {
        val message = "Tenant %s has been created.".formatted(
            ctx.highlight(tenantName)
        );

        val data = mkData(tenantName, true, currStatus);

        return OutputAll.response(message, data, List.of(
            new Hint("Get more information about the new tenant:", "${cli.name} streaming get %s".formatted(tenantName))
        ));
    }

    private LinkedHashMap<String, Object> mkData(TenantName tenantName, Boolean wasCreated, TenantStatus currentStatus) {
        return sequencedMapOf(
            "tenantName", tenantName,
            "wasCreated", wasCreated,
            "currentStatus", currentStatus
        );
    }
}
