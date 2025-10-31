package com.dtsx.astra.cli.unit.core.completions;

import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext;
import com.dtsx.astra.cli.testlib.extensions.context.UseTestCtx;
import net.jqwik.api.Group;
import net.jqwik.api.PropertyDefaults;

@Group
@PropertyDefaults(tries = 10)
public class CompletionsCacheTest {
    @UseTestCtx(fs = "jimfs")
    private TestCliContext ctx;

//    @Group
//    class update {
//        @Example
//        public void nothing_happens_if_no_cache_dir(@UseTestCtx TestCliContext ctxUsingDummyFs) {
//            val instance = new CompletionsCache(ctxUsingDummyFs.get()) {
//                @Override
//                protected Optional<Path> useCacheDir() {
//                    return Optional.empty();
//                }
//
//                @Override
//                protected String useCacheFileName() {
//                    return "cache-file";
//                }
//            };
//
//            // default dummy file system errors whenever it's used;
//            // if we call `.update()` and no error from accessing the file system is thrown,
//            // then it must mean we just returned immediately
//            instance.update((_) -> Set.of(UUID.randomUUID().toString()));
//        }
//
//        @Example
//        public void creates_cache_file_if_it_does_not_exist() {
//            val instance = mkBasicCompletionsCache();
//
//            instance.update((_) -> Set.of(UUID.randomUUID().toString()));
//
//            assertThat(instance.resolveCacheFile()).hasValueSatisfying((path) -> {
//                assertThat(path).isNotEmptyFile(); // this also checks that the file exists and is a regular file
//                assertThat(path).hasFileName("cache-file");
//                assertThat(path).hasParent(ctx.get().home().dirs().useCompletionsCache());
//            });
//        }
//
//        @Property
//        public void updates_cache_file_based_on_existing_content(@ForAll @Size(min = 1) Set<String> _existing, @ForAll @Size(min = 1) Set<String> _updated) {
//            val instance = mkBasicCompletionsCache();
//
//            val existing = new TreeSet<>(_existing); // just for predictable ordering for verification purposes
//            val updated = new TreeSet<>(_updated);
//
//            instance.update((_) -> existing);
//
//            assertThat(instance.resolveCacheFile()).hasValueSatisfying((path) -> {
//                assertThat(path).isRegularFile();
//                assertThat(path).hasContent(String.join(NL, existing.stream().map(JsonUtils::writeValue).toList()));
//            });
//
//            instance.update((before) -> new TreeSet<>(before) {{ addAll(updated); }});
//
//            assertThat(instance.resolveCacheFile()).hasValueSatisfying((path) -> {
//                assertThat(path).isRegularFile();
//                assertThat(path).hasContent(String.join(NL, new TreeSet<>(existing) {{ addAll(updated); }}.stream().map(JsonUtils::writeValue).toList()));
//            });
//        }
//
//        @Example
//        public void does_not_create_cache_file_if_no_candidates() {
//            val instance = mkBasicCompletionsCache();
//
//            instance.update((_) -> Set.of());
//
//            assertThat(instance.resolveCacheFile()).hasValueSatisfying((path) -> {
//                assertThat(path).doesNotExist();
//            });
//        }
//
//        @Property
//        public void deletes_cache_file_if_empty(@ForAll @Size(min = 1) Set<String> existing) {
//            val instance = mkBasicCompletionsCache();
//
//            instance.update((_) -> existing);
//
//            assertThat(instance.resolveCacheFile()).hasValueSatisfying((path) -> {
//                assertThat(path).isNotEmptyFile();
//            });
//
//            instance.update((_) -> Set.of());
//
//            assertThat(instance.resolveCacheFile()).hasValueSatisfying((path) -> {
//                assertThat(path).doesNotExist();
//            });
//        }
//
//        private CompletionsCache mkBasicCompletionsCache() {
//            return new CompletionsCache(ctx.get()) {
//                @Override
//                protected String useCacheFileName() {
//                    return "cache-file";
//                }
//            };
//        }
//    }

    @Group
    class resolveCacheFile {

    }
}
