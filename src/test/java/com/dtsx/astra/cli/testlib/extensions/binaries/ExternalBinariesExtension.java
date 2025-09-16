package com.dtsx.astra.cli.testlib.extensions.binaries;

import com.dtsx.astra.cli.core.CliProperties;
import com.dtsx.astra.cli.core.completions.impls.NoopCompletionsCache;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.gateways.GatewayProviderImpl;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.testlib.CloseableReentrantLock;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;
import com.dtsx.astra.cli.testlib.TestConfig;
import com.dtsx.astra.cli.testlib.extensions.ExtensionUtils;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext;
import com.dtsx.astra.cli.utils.DbUtils;
import lombok.Cleanup;
import lombok.val;
import org.graalvm.collections.Pair;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static com.dtsx.astra.cli.testlib.extensions.context.TestCliContextOptions.emptyTestCliContextOptionsBuilder;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

// uses inspiration from https://stackoverflow.com/a/65897949
public class ExternalBinariesExtension implements ParameterResolver, TestInstancePostProcessor {
    private enum BinaryName { CQLSH, DSBULK, PULSAR, SCB }

    @Override
    public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) throws ParameterResolutionException {
        val parameter = pc.getParameter();

        return parameter.isAnnotationPresent(MockInstall.class)
            && parameter.getType() == Consumer.class;
    }

    @Override
    public Object resolveParameter(ParameterContext pc, ExtensionContext ec) throws ParameterResolutionException {
        return mkMockConsumer(pc.getParameter().getAnnotation(MockInstall.class).value(), ec);
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext ec) throws Exception {
        for (val field : ExtensionUtils.getAllFields(testInstance)) {
            if (field.isAnnotationPresent(MockInstall.class) && field.getType() == Consumer.class) {
                field.setAccessible(true);
                field.set(testInstance, mkMockConsumer(field.getAnnotation(MockInstall.class).value(), ec));
            }
        }
    }

    private static final Map<BinaryName, CloseableReentrantLock> LOCKS = Arrays.stream(BinaryName.values())
        .collect(HashMap::new, (m, v) -> m.put(v, new CloseableReentrantLock()), Map::putAll);

    private Consumer<DownloadsGateway> mkMockConsumer(String name, ExtensionContext ec) {
        val asBinaryName = BinaryName.valueOf(name.toUpperCase());

        @Cleanup val _lock = LOCKS.get(asBinaryName).use();

        val binary = getFromJunitStore(asBinaryName, ec).orElseGet(() -> {
            val downloaded = downloadBinary(asBinaryName);
            return addToJunitStore(Pair.create(downloaded, asBinaryName), ec);
        });

        return switch (asBinaryName) {
            case CQLSH -> (mock) -> {
                when(mock.cqlshPath(CliProperties.cqlsh())).thenReturn(Optional.of(binary));
                doReturn(Either.pure(binary)).when(mock).downloadCqlsh(CliProperties.cqlsh());
            };
            case DSBULK -> (mock) -> {
                when(mock.dsbulkPath(CliProperties.dsbulk())).thenReturn(Optional.of(binary));
                doReturn(Either.pure(binary)).when(mock).downloadDsbulk(CliProperties.dsbulk());
            };
            case PULSAR -> (mock) -> {
                when(mock.pulsarShellPath(CliProperties.pulsar())).thenReturn(Optional.of(binary));
                doReturn(Either.pure(binary)).when(mock).downloadPulsarShell(CliProperties.pulsar());
            };
            case SCB -> (mock) -> {
                for (val ref : List.of(Databases.NameRef, Databases.IdRef)) {
                    doReturn(Either.pure(List.of(binary))).when(mock).downloadCloudSecureBundles(ref, Databases.NameRef.toString(), Set.of(DbUtils.resolveDatacenter(Databases.One, Optional.empty())));
                }
            };
        };
    }

    private Path downloadBinary(BinaryName name) {
        val result = switch (name) {
            case CQLSH -> getDownloadsGateway().downloadCqlsh(CliProperties.cqlsh());
            case DSBULK -> getDownloadsGateway().downloadDsbulk(CliProperties.dsbulk());
            case PULSAR -> getDownloadsGateway().downloadPulsarShell(CliProperties.pulsar());
            case SCB -> {
                val db = getDbGateway().findOne(TestConfig.dbId());
                val paths = getDownloadsGateway().downloadCloudSecureBundles(TestConfig.dbId(), db.getInfo().getName(), Set.of(DbUtils.resolveDatacenter(db, Optional.of(TestConfig.dbRegion()))));
                yield paths.map(List::getFirst);
            }
        };

        if (result.isLeft()) {
            throw new RuntimeException("Failed to download " + name + ": " + result.getLeft());
        }

        return result.getRight();
    }

    private Path addToJunitStore(Pair<Path, BinaryName> binaryAndName, ExtensionContext ec) {
        ec.getRoot().getStore(Namespace.create(getClass(), binaryAndName.getRight()))
            .put("cliContext", binaryAndName.getLeft());

        return binaryAndName.getLeft();
    }

    private Optional<Path> getFromJunitStore(BinaryName name, ExtensionContext ec) {
        val gotten = ec.getRoot().getStore(Namespace.create(getClass(), name))
            .get("cliContext", Path.class);

        return Optional.ofNullable(gotten);
    }

    private @Nullable DbGateway cachedDbGateway;
    private @Nullable DownloadsGateway cachedDownloadsGateway;

    private DbGateway getDbGateway() {
        if (cachedDbGateway != null) {
            return cachedDbGateway;
        }

        try (val ctx = new TestCliContext(emptyTestCliContextOptionsBuilder().useRealFs())) {
            return cachedDbGateway = new GatewayProviderImpl().mkDbGateway(TestConfig.token(), TestConfig.env(), NoopCompletionsCache.INSTANCE, ctx.get());
        }
    }

    private DownloadsGateway getDownloadsGateway() {
        if (cachedDownloadsGateway != null) {
            return cachedDownloadsGateway;
        }

        try (val ctx = new TestCliContext(emptyTestCliContextOptionsBuilder().useRealFs())) {
            return cachedDownloadsGateway = new GatewayProviderImpl().mkDownloadsGateway(ctx.get());
        }
    }
}
