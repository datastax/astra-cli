//package com.dtsx.astra.cli.unit.core.parsers.env;
//
//import com.dtsx.astra.cli.core.parsers.env.EnvFile;
//import com.dtsx.astra.cli.core.parsers.env.EnvParser;
//import com.dtsx.astra.cli.core.parsers.env.ast.EnvComment;
//import com.dtsx.astra.cli.core.parsers.env.ast.EnvKVPair;
//import lombok.SneakyThrows;
//import lombok.val;
//import net.jqwik.api.*;
//import org.graalvm.collections.Pair;
//
//import java.util.ArrayList;
//import java.util.Scanner;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//public class EnvFileTest {
//    @Group
//    public class parse {
//        @Property
//        @SneakyThrows
//        public void parses_valid_files_correctly(@ForAll("parseArb") Pair<EnvFile, String> pair) {
//            assertThat(new EnvParser().parseEnvFile(new Scanner(pair.getRight()))).isEqualTo(pair.getLeft());
//        }
//
//        @Provide
//        private Arbitrary<Pair<EnvFile, String>> parseArb() {
//            val initArbs = Combinators.combine(
//                envFileArb(),
//                Arbitraries.randoms()
//            );
//
//            return initArbs.as((file, rand) -> {
//                return Pair.create(file, sb.toString());
//            });
//        }
//    }
//
////    @Group
////    public class render {
////        @Property
////        @SneakyThrows
////        public void renders_files_correctly(@ForAll("renderArb") Pair<String, IniFile> pair) {
////            assertThat(pair.getRight().render(false)).isEqualTo(pair.getLeft().trim());
////        }
////    }
//
//    private Arbitrary<EnvFile> envFileArb() {
//        val nodes = Arbitraries.integers().list().flatMapEach((_, i) -> {
//            return switch (i % 3) {
//                case 0 -> {
//                    yield Combinators.combine(
//                        Arbitraries.strings().alpha().ofMinLength(1).excludeChars('='),
//                        Arbitraries.strings().alpha().ofMinLength(1).excludeChars('=')
//                    ).as(EnvKVPair::new);
//                }
//                case 1 -> {
//                    yield Arbitraries.strings().alpha().ofMinLength(1).map(s -> " ".repeat(i % 10) + "#" + " ".repeat(i % 7) + s).map(EnvComment::new);
//                }
//                default -> {
//                    yield Arbitraries.strings().whitespace().map(s -> new EnvKVPair(s, "value"));
//                }
//            };
//        });
//
//        return nodes.map(n -> new EnvFile(new ArrayList<>(n)));
//    }
//}
