package com.dtsx.astra.cli.unit.core.completions;

import com.dtsx.astra.cli.core.completions.CompletionsCache;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext;
import com.dtsx.astra.cli.testlib.extensions.context.UseTestCtx;
import com.dtsx.astra.cli.utils.JsonUtils;
import lombok.val;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

import static com.dtsx.astra.cli.utils.CollectionUtils.listConcat;
import static com.dtsx.astra.cli.utils.MiscUtils.also;
import static org.assertj.core.api.Assertions.assertThat;

@Group
@PropertyDefaults(tries = 10)
public class CompletionsCacheTest {
    @UseTestCtx(fs = "jimfs")
    private TestCliContext ctx;

    @Group
    class fileCreation {
        @Property
        public void creates_cache_file_if_it_does_not_exist_and_valid_candidates(
            @ForAll("fileName") String cacheFileName,
            @ForAll @NotEmpty List<@NotBlank String> cs,
            @ForAll("appendCache") CacheSetter cacheSetter
        ) {
            val instance = also(mkBasicCompletionsCache(cacheFileName), (it) -> cacheSetter.accept(it, cs));

            val primaryCacheFile = instance.primaryCacheFile().orElseThrow();
            assertThat(primaryCacheFile).isNotEmptyFile(); // this also checks that the file exists and is a regular file
            assertThat(primaryCacheFile).hasFileName(cacheFileName);
            assertThat(primaryCacheFile).hasParent(ctx.get().home().dirs().useCompletionsCache());
        }

        @Property
        public void does_not_create_cache_file_if_no_candidates(
            @ForAll("fileName") String cacheFileName,
            @ForAll List<@CharRange(from = ' ', to = ' ') String> cs,
            @ForAll("appendCache") CacheSetter cacheSetter
        ) {
            val instance = also(mkBasicCompletionsCache(cacheFileName), (it) -> cacheSetter.accept(it, cs));

            val primaryCacheFile = instance.primaryCacheFile().orElseThrow();
            assertThat(primaryCacheFile).doesNotExist();
        }

        @Property
        public void does_not_create_cache_file_if_no_candidates(
            @ForAll("fileName") String cacheFileName,
            @ForAll @NotEmpty List<@NotBlank String> cs,
            @ForAll("deleteCache") CacheDeleter cacheDeleter
        ) {
            val instance = mkBasicCompletionsCache(cacheFileName);

            val primaryCacheFile = instance.primaryCacheFile().orElseThrow();
            assertThat(primaryCacheFile).doesNotExist();

            cacheDeleter.accept(instance, cs);
        }
    }

    @Group
    class fileDeletion {
        @Property
        public void deletes_cache_file_when_no_candidates(
            @ForAll("fileName") String cacheFileName,
            @ForAll @NotEmpty List<@NotBlank String> cs,
            @ForAll("appendCache") CacheSetter cacheSetter,
            @ForAll("deleteCache") CacheDeleter cacheDeleter
        ) {
            val instance = also(mkBasicCompletionsCache(cacheFileName), (it) -> cacheSetter.accept(it, cs));

            val primaryCacheFile = instance.primaryCacheFile().orElseThrow();
            assertThat(primaryCacheFile).isNotEmptyFile();

            cacheDeleter.accept(instance, cs);

            assertThat(primaryCacheFile).doesNotExist();
        }
    }

    @Group
    class fileContent {
        @Property
        public void updates_cache_file_based_on_existing_content(
            @ForAll("fileName") String cacheFileName,
            @ForAll @Size(min = 1) @UniqueElements List<@NotBlank String> existing,
            @ForAll @Size(min = 1) @UniqueElements List<@NotBlank String> updated
        ) {
            val instance = mkBasicCompletionsCache(cacheFileName);
            val primaryCacheFile = instance.primaryCacheFile().orElseThrow();

            instance.setCache(existing);

            assertThat(primaryCacheFile).isRegularFile();
            assertThat(primaryCacheFile).content().satisfies((content) -> {
                assertThat(content.lines().toList()).containsExactlyInAnyOrderElementsOf(
                    existing.stream().map(JsonUtils::writeValue).toList()
                );
            });

            for (val item : updated) {
                instance.addToCache(item);
            }

            assertThat(primaryCacheFile).isRegularFile();
            assertThat(primaryCacheFile).content().satisfies((content) -> {
                assertThat(content.lines().toList()).containsExactlyInAnyOrderElementsOf(
                    listConcat(existing, updated).stream().distinct().map(JsonUtils::writeValue).toList()
                );
            });
        }
    }

    @Provide
    private Arbitrary<String> fileName() {
        return Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(20);
    }

    interface CacheSetter extends BiConsumer<CompletionsCache, List<String>> {}
    interface CacheDeleter extends BiConsumer<CompletionsCache, List<String>> {}

    @Provide
    private Arbitrary<CacheSetter> appendCache() {
        return Arbitraries.of(
            this::appendCacheViaSet,
            this::appendCacheViaAdd
        );
    }

    @Provide
    private Arbitrary<CacheDeleter> deleteCache() {
        return Arbitraries.of(
            this::deleteCacheViaSet,
            this::deleteCacheViaRm
        );
    }

    private void appendCacheViaSet(CompletionsCache instance, List<String> toAppend) {
        instance.setCache(toAppend);
    }

    private void appendCacheViaAdd(CompletionsCache instance, List<String> toAppend) {
        toAppend.forEach(instance::addToCache);
    }

    private void deleteCacheViaSet(CompletionsCache instance, List<String> toDelete) {
        instance.setCache(List.of());
    }

    private void deleteCacheViaRm(CompletionsCache instance, List<String> toDelete) {
        toDelete.forEach(instance::removeFromCache);
    }

    private BasicCompletionsCache mkBasicCompletionsCache(String cacheFileName) {
        return new BasicCompletionsCache(ctx.get(), cacheFileName);
    }

    private static class BasicCompletionsCache extends CompletionsCache {
        private final Path primaryCacheFile;

        BasicCompletionsCache(com.dtsx.astra.cli.core.CliContext ctx, String cacheFileName) {
            super(ctx);
            this.primaryCacheFile = defaultCacheDir(ctx).resolve(cacheFileName);
        }

        @Override
        protected java.util.Optional<Path> primaryCacheFile() {
            return java.util.Optional.of(primaryCacheFile);
        }

        @Override
        protected List<Path> mirrorCacheFiles() {
            return List.of();
        }
    }
}
