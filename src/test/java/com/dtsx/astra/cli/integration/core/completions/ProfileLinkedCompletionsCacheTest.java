package com.dtsx.astra.cli.integration.core.completions;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileSource;
import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileSource.DefaultFile;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.completions.ProfileLinkedCompletionsCache;
import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.testlib.Fixtures;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext;
import com.dtsx.astra.cli.testlib.extensions.context.UseTestCtx;
import com.dtsx.astra.cli.utils.JsonUtils;
import lombok.SneakyThrows;
import lombok.val;
import net.jqwik.api.*;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class ProfileLinkedCompletionsCacheTest {
    @UseTestCtx(fs = "jimfs")
    private TestCliContext ctx;

    @Property
    public void test_lifecycle(@ForAll("completions") List<String> completions) {
        val config = mkTestConfig();
        val cache = new DynamicCompletionsCache(ctx.get(), config.profileAndSource);

        forEachPath(cache, (path) -> {
            assertThat(path).doesNotExist();
        });

        test_cache_still_does_not_exist_with_meaningless_updates(cache, completions);
        test_cache_creation_and_population(cache, completions);
        test_copying_profile(cache, config);
        test_deleting_profile(cache, config);
    }

    private void test_cache_still_does_not_exist_with_meaningless_updates(ProfileLinkedCompletionsCache cache, List<String> completions) {
        cache.setCache(List.of());

        forEachPath(cache, (path) -> {
            assertThat(path).doesNotExist();
        });

        for (val completion : completions) {
            cache.removeFromCache(completion);
        }

        forEachPath(cache, (path) -> {
            assertThat(path).doesNotExist();
        });
    }

    @SneakyThrows
    private void test_cache_creation_and_population(ProfileLinkedCompletionsCache cache, List<String> completions) {
        // populate via set
        cache.setCache(completions);

        val primaryCacheFile = cache.primaryCacheFile().orElseThrow();
        val mirrorCacheFiles = cache.mirrorCacheFiles();

        val initialContent = Files.readString(primaryCacheFile);
        val completionsSet = new HashSet<>(completions);

        assertThat(initialContent.lines().toList())
            .hasSize(completionsSet.size())
            .extracting(s -> JsonUtils.readValue(s, String.class))
            .containsExactlyInAnyOrderElementsOf(completionsSet);

        // Check primary cache exists
        assertThat(primaryCacheFile).exists().content().isEqualTo(initialContent);

        // Check mirror caches have same content as primary
        for (val mirrorCacheFile : mirrorCacheFiles) {
            assertThat(mirrorCacheFile).exists().content().isEqualTo(initialContent);
        }

        // reset cache
        for (val completion : completions) {
            cache.removeFromCache(completion);
        }

        assertThat(primaryCacheFile).doesNotExist();
        for (val mirrorCacheFile : mirrorCacheFiles) {
            assertThat(mirrorCacheFile).doesNotExist();
        }

        // populate via add
        for (val completion : completions) {
            cache.addToCache(completion);
        }

        assertThat(primaryCacheFile).exists().content().isEqualTo(initialContent);
        for (val mirrorCacheFile : mirrorCacheFiles) {
            assertThat(mirrorCacheFile).exists().content().isEqualTo(initialContent);
        }
    }

    private void test_copying_profile(ProfileLinkedCompletionsCache cache, TestConfig config) {
        val expectedPath = completionsDir().resolve("copied-profile").resolve("db_names");
        assertThat(expectedPath).doesNotExist();

        val target = ProfileName.mkUnsafe("copied-profile");

        config.astraConfig.modify((ctx) -> {
            ctx.copyProfile(config.profileAndSource.getLeft(), target);
        });

        val created = config.astraConfig.lookupProfile(target).orElseThrow();

        assertThat(created.sourceForDefault()).isEqualTo(config.profileAndSource.getLeft().name());

        forEachPath(cache, (path) -> {
            assertThat(expectedPath).exists().hasSameTextualContentAs(path);
        });
    }

    private void test_deleting_profile(ProfileLinkedCompletionsCache cache, TestConfig config) {
        val target = ProfileName.mkUnsafe("my-profile");

        forEachPath(cache, (path) -> {
            assertThat(path).exists();
        });

        config.astraConfig.modify((ctx) -> {
            ctx.deleteProfile(target);
        });

        assertThat(defaultCacheFile()).exists().content().isNotBlank();
        assertThat(myProfileCacheFile()).doesNotExist();
    }

    private Path completionsDir() {
        return ctx.get().properties().homeFolderLocations(ctx.get().isWindows()).preferred(ctx.get()).resolve("completions-cache");
    }

    private Path defaultCacheFile() {
        return completionsDir().resolve("default").resolve("db_names");
    }

    private Path myProfileCacheFile() {
        return completionsDir().resolve("my-profile").resolve("db_names");
    }

    private void forEachPath(ProfileLinkedCompletionsCache cache, Consumer<Path> consumer) {
        assertThat(cache.primaryCacheFile()).hasValue(defaultCacheFile());
        assertThat(cache.mirrorCacheFiles()).containsExactly(myProfileCacheFile());

        cache.primaryCacheFile().ifPresent(consumer);

        for (val mirrorCacheFile : cache.mirrorCacheFiles()) {
            consumer.accept(mirrorCacheFile);
        }
    }

    private record TestConfig(AstraConfig astraConfig, Pair<Profile, ProfileSource> profileAndSource) {}

    @SneakyThrows
    private TestConfig mkTestConfig() {
        val cfgPath = AstraConfig.resolveDefaultAstraConfigFile(ctx.get());

        Files.createDirectories(cfgPath.getParent());
        Files.writeString(cfgPath, """
          [default]
          ASTRA_DB_APPLICATION_TOKEN=%s
          PROFILE_SOURCE=my-profile
        
          [my-profile]
          ASTRA_DB_APPLICATION_TOKEN=%s
        """.formatted(Fixtures.Token.unsafeUnwrap(), Fixtures.Token.unsafeUnwrap()));

        val config = AstraConfig.readAstraConfigFile(ctx.get(), null, false);
        val profile = config.lookupProfile(ProfileName.DEFAULT).orElseThrow();

        return new TestConfig(
            config,
            Pair.of(
                profile,
                new DefaultFile(profile.name().orElseThrow())
            )
        );
    }

    @Provide
    private Arbitrary<List<String>> completions() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10).filter(s -> !s.isBlank()).list().ofMinSize(1).ofMaxSize(10);
    }

    private static class DynamicCompletionsCache extends ProfileLinkedCompletionsCache {
        public DynamicCompletionsCache(CliContext ctx, Pair<Profile, ProfileSource> profileAndSource) {
            super(ctx, profileAndSource);
        }

        @Override
        protected String useCacheFileName() {
            return "db_names";
        }
    }
}
