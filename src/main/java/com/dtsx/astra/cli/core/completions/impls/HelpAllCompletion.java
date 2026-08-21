package com.dtsx.astra.cli.core.completions.impls;

import com.dtsx.astra.cli.core.completions.StaticCompletion;

import java.util.List;

public class HelpAllCompletion extends StaticCompletion {
    public HelpAllCompletion() {
        super(List.of("all"));
    }
}
