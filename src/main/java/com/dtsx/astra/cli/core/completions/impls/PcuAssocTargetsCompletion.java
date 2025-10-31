package com.dtsx.astra.cli.core.completions.impls;

// Same as DbNamesCompletion for the time being while only datacenters are
// valid targets for PCU groups. May change IF streaming associations are supported.
public class PcuAssocTargetsCompletion extends DbNamesCompletion {
    static {
        register(new PcuAssocTargetsCompletion());
    }
}
