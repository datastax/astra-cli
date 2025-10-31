package com.dtsx.astra.cli.core.completions.impls;

import com.dtsx.astra.cli.core.completions.DynamicCompletion;

public class PcuGroupsCompletion extends DynamicCompletion {
    static {
        register(new PcuGroupsCompletion());
    }

    public PcuGroupsCompletion() {
        super("""
          OUT=( $(cat "$(get_astra_dir)/completions-cache/$(get_profile)/pcu_groups" 2>/dev/null | tr '\\n' ' ') )
        """);
    }
}
