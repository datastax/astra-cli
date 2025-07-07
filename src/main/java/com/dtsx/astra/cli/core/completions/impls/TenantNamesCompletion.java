package com.dtsx.astra.cli.core.completions.impls;

import com.dtsx.astra.cli.core.completions.DynamicCompletion;

public class TenantNamesCompletion extends DynamicCompletion {
    static {
        register(new TenantNamesCompletion());
    }

    public TenantNamesCompletion() {
        super("""
          OUT=( $(cat "$(get_astra_dir)/completions-cache/$(get_profile)/tenant_names" 2>/dev/null | tr '\\n' ' ') )
        """);
    }
}
