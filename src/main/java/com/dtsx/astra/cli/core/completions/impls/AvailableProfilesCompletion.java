package com.dtsx.astra.cli.core.completions.impls;

import com.dtsx.astra.cli.core.completions.DynamicCompletion;

public class AvailableProfilesCompletion extends DynamicCompletion {
    static {
        register(new AvailableProfilesCompletion());
    }

    public AvailableProfilesCompletion() {
        super("""
          RC_FILE=$(get_astra_rc);
          OUT=();
        
          if [ -f "$RC_FILE" ]; then
            while IFS= read -r line; do
              OUT+=("$line");
            done < <(grep '^\\[.*\\]$' "$RC_FILE" | tr -d '[]');
          fi
        """);
    }
}
