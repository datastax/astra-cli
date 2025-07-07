package com.dtsx.astra.cli.core.completions.impls;

import com.dtsx.astra.cli.core.completions.DynamicCompletion;

public class DbNamesCompletion extends DynamicCompletion {
    static {
        register(new DbNamesCompletion());
    }

    public DbNamesCompletion() {
        super("""
          OUT=( $(cat "$(get_astra_dir)/completions-cache/$(get_profile)/db_names" 2>/dev/null | tr '\\n' ' ') )
        """);
    }
}
