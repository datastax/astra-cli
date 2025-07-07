package com.dtsx.astra.cli.core.completions.impls;

import com.dtsx.astra.cli.core.completions.DynamicCompletion;

public class UserEmailsCompletion extends DynamicCompletion {
    static {
        register(new UserEmailsCompletion());
    }

    public UserEmailsCompletion() {
        super("""
          OUT=( $(cat "$(get_astra_dir)/completions-cache/$(get_profile)/user_emails" 2>/dev/null | tr '\\n' ' ') )
        """);
    }
}
