package com.dtsx.astra.cli.core.output.prompters.specific;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsClearAfterSelection;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsFallback;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.dtsx.astra.cli.core.output.ExitCode.PROFILE_NOT_FOUND;

public class ProfileNamePrompter {
    public static String prompt(CliContext ctx, List<Profile> candidates, String prompt, UnaryOperator<NEList<Profile>> modifier, Function<NeedsFallback<Profile>, NeedsClearAfterSelection<Profile>> fix) {
        return SpecificPrompter.<Profile, String>run(ctx, (b) -> b
            .thing("profile")
            .prompt(prompt)
            .thingsSupplier(() -> candidates)
            .thingNotFoundCode(PROFILE_NOT_FOUND)
            .getThingIdentifier(p -> p.nameOrDefault().unwrap())
            .getThingDisplayExtra((p, _) -> p.env().name().toLowerCase())
            .modifier(modifier)
            .fix(fix)
            .mapSingleFound(p -> p.nameOrDefault().unwrap())
            .mapMultipleFound(p -> p.nameOrDefault().unwrap())
        );
    }
}
