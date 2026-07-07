package com.dtsx.astra.cli.core.completions.impls;

import com.dtsx.astra.cli.core.completions.DynamicCompletion;

public class AvailableProfilesNoDefaultCompletion extends DynamicCompletion {
    static {
        register(new AvailableProfilesNoDefaultCompletion());
    }

    public AvailableProfilesNoDefaultCompletion() {
        super("""
          RC_FILE=$(get_astra_rc);
          OUT=();
        
          if [ -f "$RC_FILE" ]; then
            while IFS= read -r line; do
              if [[ "$line" != "default" ]]; then
                  OUT+=("$line");
              fi
            done < <(grep '^\\[.*\\]$' "$RC_FILE" | tr -d '[]');
          fi
        """);
    }
}
