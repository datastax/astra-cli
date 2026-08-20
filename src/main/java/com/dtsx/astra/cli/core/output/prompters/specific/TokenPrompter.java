package com.dtsx.astra.cli.core.output.prompters.specific;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsClearAfterSelection;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsFallback;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.utils.StringUtils;
import com.dtsx.astra.sdk.org.domain.IamToken;

import java.util.function.Function;

import static com.dtsx.astra.cli.core.output.ExitCode.TOKEN_NOT_FOUND;
import static java.util.Objects.requireNonNullElse;

public class TokenPrompter {
    public static String prompt(CliContext ctx, TokenGateway gateway, String prompt, Function<NeedsFallback<IamToken>, NeedsClearAfterSelection<IamToken>> fix) {
        return SpecificPrompter.<IamToken, String>run(ctx, (b) -> b
            .thing("token")
            .prompt(prompt)
            .thingNotFoundCode(TOKEN_NOT_FOUND)
            .thingsSupplier(() -> gateway.findAll().toList())
            .getThingIdentifier(IamToken::getClientId)
            .getThingDisplayExtra((token, _) -> StringUtils.truncate(requireNonNullElse(token.getDescription().trim(), ""), 30))
            .fix(fix)
            .mapSingleFound(IamToken::getClientId)
        );
    }
}
