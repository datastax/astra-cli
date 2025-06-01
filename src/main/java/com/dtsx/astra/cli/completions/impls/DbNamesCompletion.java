package com.dtsx.astra.cli.completions.impls;

import com.dtsx.astra.cli.completions.DynamicCompletion;

public class DbNamesCompletion extends DynamicCompletion {
    public DbNamesCompletion() {
        super("cat \"$(get_astra_dir)/completions-cache/$(get_profile)/db_names\" 2>/dev/null", (fns) -> fns.GET_PROFILE + fns.GET_ASTRA_DIR);
    }
}
