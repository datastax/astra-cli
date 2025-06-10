package com.dtsx.astra.cli.core.completions;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public abstract class DynamicCompletion implements Iterable<String> {
    public enum UtilityFunctions {
        INSTANCE;
        public final String GET_PROFILE = "function get_profile(){ for ((i=0;i<${#COMP_WORDS[@]};i++));do [[ ${COMP_WORDS[i]} == --profile ]]&&((i+1<${#COMP_WORDS[@]}))&&echo ${COMP_WORDS[i+1]}&&return;done; echo default;};";
        public final String GET_ASTRA_DIR = "function get_astra_dir(){ echo ~/.astra;};";
    }

    private final String bash;

    public DynamicCompletion(String bash) {
        this(bash, (_) -> "");
    }

    public DynamicCompletion(String bash, Function<UtilityFunctions, String> funcs) {
        this.bash = "\"`" + funcs.apply(UtilityFunctions.INSTANCE) + bash + "`\"";
    }

    @Override
    public @NotNull Iterator<String> iterator() {
        return List.of(bash).iterator();
    }
}
