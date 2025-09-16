package com.dtsx.astra.cli.testlib.extensions.context;

import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.cli.testlib.TestConfig;
import com.dtsx.astra.cli.testlib.doubles.DummyFileSystem;
import com.dtsx.astra.cli.testlib.doubles.GatewayProviderMock;
import com.google.common.jimfs.Jimfs;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;
import lombok.val;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.mockito.Mockito.*;

@With
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class TestCliContextOptions {
    private final List<String> stdin;
    private final GatewayProviderMock gateways;
    private final List<Consumer<GatewayProviderMock>> verifyFns;
    private final FileSystem fs;
    private final OutputType outputType;
    private final Optional<Profile> forceProfile;
    private final Optional<Function<FileSystem, Path>> homeDir;
    private final Map<String, Object> extra;

    public interface TestCliContextOptionsModifier extends Function<TestCliContextOptionsBuilder<?, TestCliContextOptions>, TestCliContextOptionsBuilder<?, TestCliContextOptions>> {}

    @RequiredArgsConstructor
    public static class TestCliContextOptionsBuilder<Builder extends TestCliContextOptionsBuilder<Builder, Opts>, Opts extends TestCliContextOptions> {
        protected final Opts options;

        @SuppressWarnings("unchecked")
        public Builder use(Function<Builder, Builder> modifier) {
            return modifier.apply((Builder) this);
        }

        public Builder stdin(List<String> lines) {
            return mkSelf(options.withStdin(lines));
        }

        public Builder stdin(String... lines) {
            return stdin(List.of(lines));
        }

        public <G extends SomeGateway> Builder gateway(Class<G> clazz) {
            return gateway(clazz, (_) -> {});
        }

        public <G extends SomeGateway> Builder gateway(Class<G> clazz, Consumer<G> withInstance) {
            val instance = mock(clazz, withSettings().defaultAnswer(RETURNS_SMART_NULLS));
            withInstance.accept(instance);
            return mkSelf(options.withGateways(options.gateways().withInstance(instance)));
        }

        public Builder verify(Consumer<GatewayProviderMock> fn) {
            val newVerifyFns = new ArrayList<>(options.verifyFns());
            newVerifyFns.add(fn);
            return mkSelf(options.withVerifyFns(newVerifyFns));
        }

        public Builder useJimfs() {
            return mkSelf(options.withFs(Jimfs.newFileSystem()));
        }

        public Builder useRealFs() {
            return mkSelf(options.withFs(FileSystems.getDefault()));
        }

        public Builder useHomeDir(Function<FileSystem, Path> fn) {
            return mkSelf(options.withHomeDir(Optional.of(fn)));
        }

        public Builder outputType(OutputType outputType) {
            return mkSelf(options.withOutputType(outputType));
        }

        public Builder extra(String key, Object value) {
            val newExtra = new HashMap<>(options.extra());
            newExtra.put(key, value);
            return mkSelf(options.withExtra(newExtra));
        }

        public Builder forceProfile(Profile profile) {
            return mkSelf(options.withForceProfile(Optional.of(profile)));
        }

        public Opts build() {
            if (options.fs() == FileSystems.getDefault() && options.homeDir().isEmpty()) { // prevent messing with real astra home folder by accident
                return useHomeDir(TestConfig::astraHome).build();
            }
            return options;
        }

        @SuppressWarnings("unchecked")
        protected Builder mkSelf(TestCliContextOptions options) {
            return (Builder) new TestCliContextOptionsBuilder<>(options);
        }
    }

    public static TestCliContextOptionsBuilder<?, TestCliContextOptions> emptyTestCliContextOptionsBuilder() {
        return new TestCliContextOptionsBuilder<>(new TestCliContextOptions(
            List.of(),
            new GatewayProviderMock(),
            List.of(),
            DummyFileSystem.INSTANCE,
            OutputType.HUMAN,
            Optional.empty(),
            Optional.empty(),
            Map.of()
        ));
    }
}
