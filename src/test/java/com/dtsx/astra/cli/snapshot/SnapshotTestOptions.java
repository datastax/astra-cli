package com.dtsx.astra.cli.snapshot;

import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.testlib.doubles.GatewayProviderMock;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContextOptions;
import lombok.val;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class SnapshotTestOptions extends TestCliContextOptions {
    public static final String EXTRA_COMMENTS_KEY = "SnapshotTestOptions.comments";

    public SnapshotTestOptions(List<String> stdin, GatewayProviderMock gateways, List<Consumer<GatewayProviderMock>> verifyFns, FileSystem fs, OutputType outputType, Optional<Profile> forceProfile, Optional<Function<FileSystem, Path>> homeDir, Map<String, Object> extra) {
        super(stdin, gateways, verifyFns, fs, outputType, forceProfile, homeDir, extra);
    }

    @SuppressWarnings("unchecked")
    public List<String> comments() {
        return (List<String>) extra().getOrDefault(EXTRA_COMMENTS_KEY, new ArrayList<String>());
    }

    public interface SnapshotTestOptionsModifier extends Function<SnapshotTestOptionsBuilder, SnapshotTestOptionsBuilder> {}

    public static class SnapshotTestOptionsBuilder extends TestCliContextOptionsBuilder<SnapshotTestOptionsBuilder, TestCliContextOptions> {
        public SnapshotTestOptionsBuilder(TestCliContextOptions options) {
            super(options);
        }

        public SnapshotTestOptionsBuilder comment(String... comments) {
            // noinspection unchecked
            val allComments = new ArrayList<>((List<String>) options.extra().getOrDefault(EXTRA_COMMENTS_KEY, new ArrayList<String>()));
            allComments.addAll(List.of(comments));
            return extra(EXTRA_COMMENTS_KEY, allComments);
        }

        @Override
        protected SnapshotTestOptionsBuilder mkSelf(TestCliContextOptions options) {
            return new SnapshotTestOptionsBuilder(options);
        }

        @Override
        public SnapshotTestOptions build() {
            val built = super.build();

            return new SnapshotTestOptions(
                built.stdin(),
                built.gateways(),
                built.verifyFns(),
                built.fs(),
                built.outputType(),
                built.forceProfile(),
                built.homeDir(),
                built.extra()
            );
        }
    }

    public static SnapshotTestOptionsBuilder emptySnapshotTestOptionsBuilder() {
        return new SnapshotTestOptionsBuilder(emptyTestCliContextOptionsBuilder().build());
    }
}
