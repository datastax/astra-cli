package com.dtsx.astra.cli.core.output.prompters.strategies;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.output.prompters.PromptRequest;
import com.dtsx.astra.cli.core.output.prompters.SelectionStrategy;
import lombok.val;

import java.util.Optional;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

public class TextSelectionStrategy<T> implements SelectionStrategy<T> {
    private static final String MOVE_UP_CLEAR = "\033[1A\033[2K\r";

    private final CliContext ctx;
    private final PromptRequest.Open<T> req;

    public TextSelectionStrategy(CliContext ctx, PromptRequest.Open<T> req) {
        this.ctx = ctx;
        this.req = req;
    }

    public static class Meta implements SelectionStrategy.Meta.Open {
        @Override
        public boolean isSupported(CliContext ctx) {
            return ctx.outputIsHuman() && ctx.isTty();
        }

        @Override
        public <T> SelectionStrategy<T> mkInstance(CliContext ctx, PromptRequest.Open<T> request) {
            return new TextSelectionStrategy<>(ctx, request);
        }
    }

    @Override
    public Optional<T> select() {
        val prompt = mkPrompt();

        val result = readAnswer(prompt);

        if (req.clearAfterSelection() && ctx.ansiEnabled()) {
            clearPrompt();
        } else {
            cleanupOutput(result);
        }

        return result
            .or(req::defaultOption)
            .map(req.mapper());
    }

    private String mkPrompt() {
        return ctx.console().format(req.prompt() + NL + "@!>!@ " + (req.echoOff() ? ctx.colors().NEUTRAL_500.use("@|faint input hidden for security |@") : ""));
    }

    private Optional<String> readAnswer(String prompt) {
        return Optional.ofNullable(ctx.console().unsafeReadLine(prompt, req.echoOff())).filter(s -> !s.isBlank());
    }

    private void clearPrompt() {
        for (int i = 0; i < req.prompt().split("\n").length + 1; i++) {
            ctx.console().print(MOVE_UP_CLEAR);
        }
    }

    private void cleanupOutput(Optional<String> result) {
        if (req.echoOff() && result.isPresent() && ctx.ansiEnabled()) {
            ctx.console().print(MOVE_UP_CLEAR);
            ctx.console().println("@!>!@ " + req.displayContentWhenDone().apply(result.get()));
        }
        else if (result.isEmpty() && req.defaultOption().isPresent()) {
            ctx.console().print(MOVE_UP_CLEAR);
            ctx.console().println("@!>!@ " + req.defaultOption().get());
        }
        ctx.console().println();
    }
}
