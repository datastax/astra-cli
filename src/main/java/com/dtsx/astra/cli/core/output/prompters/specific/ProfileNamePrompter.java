package com.dtsx.astra.cli.core.output.prompters.specific;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.InvalidProfile;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsClearAfterSelection;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsFallback;

import java.util.List;
import java.util.function.Function;

import static com.dtsx.astra.cli.core.output.ExitCode.PROFILE_NOT_FOUND;

public class ProfileNamePrompter {
    public static String prompt(CliContext ctx, List<Either<InvalidProfile, Profile>> candidates, String prompt, Function<NEList<Either<InvalidProfile, Profile>>, NEList<Either<InvalidProfile, Profile>>> modifier, Function<NeedsFallback<Either<InvalidProfile, Profile>>, NeedsClearAfterSelection<Either<InvalidProfile, Profile>>> fix) {
        return SpecificPrompter.<Either<InvalidProfile, Profile>, String>run(ctx, (b) -> b
            .thing("profile")
            .prompt(prompt)
            .thingsSupplier(() -> candidates)
            .thingNotFoundCode(PROFILE_NOT_FOUND)
            .getThingIdentifier(ProfileNamePrompter::extractProfileName)
            .getThingDisplayExtra((either, _) -> either.fold(
                (_) -> "@|bold,red (invalid)|@",
                (p) -> p.env().name().toLowerCase()
            ))
            .modifier(modifier)
            .fix(fix)
            .mapSingleFound(ProfileNamePrompter::extractProfileName)
            .mapMultipleFound(ProfileNamePrompter::extractProfileName)
        );
    }

    private static String extractProfileName(Either<InvalidProfile, Profile> profile) {
        return profile.fold(
            (invalid) -> invalid.section().name(),
            (valid) -> valid.nameOrDefault().unwrap()
        );
    }
}
