package com.dtsx.astra.cli.core.output.prompters.strategies;

import com.dtsx.astra.cli.core.CliEnvironment;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.core.output.prompters.PromptRequest;
import com.dtsx.astra.cli.core.output.prompters.SelectionStrategy;
import lombok.val;

import java.io.Console;
import java.util.Objects;
import java.util.Optional;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

public class TextSelectionStrategy<T> implements SelectionStrategy<T> {
    private static final String MOVE_UP_CLEAR = "\033[1A\033[2K\r";

    private final PromptRequest.Open<T> req;
    private final Console console;

    public TextSelectionStrategy(PromptRequest.Open<T> req) {
        this.req = req;
        this.console = Objects.requireNonNull(AstraConsole.getConsole());
    }

    public static class Meta implements SelectionStrategy.Meta.Open {
        @Override
        public boolean isSupported() {
            return OutputType.isHuman() && CliEnvironment.isTty();
        }

        @Override
        public <T> SelectionStrategy<T> mkInstance(PromptRequest.Open<T> request) {
            return new TextSelectionStrategy<>(request);
        }
    }

    @Override
    public Optional<T> select() {
        val prompt = mkPrompt();

        val result = readAnswer(prompt);

        if (req.clearAfterSelection() && AstraColors.enabled()) {
            clearPrompt();
        } else {
            cleanupOutput(result);
        }

        return result
            .or(req::defaultOption)
            .map(req.mapper());
    }

    private String mkPrompt() {
        return AstraConsole.format(req.prompt() + NL + "@!>!@ " + (req.echoOff() ? AstraColors.NEUTRAL_500.use("@|faint input hidden for security |@") : ""));
    }

    private Optional<String> readAnswer(String prompt) {
        val res = (req.echoOff())
            ? Optional.ofNullable(console.readPassword(prompt)).map(String::valueOf)
            : Optional.ofNullable(console.readLine(prompt));

        return res.filter(s -> !s.isBlank());
    }

    private void clearPrompt() {
        for (int i = 0; i < req.prompt().split("\n").length + 1; i++) {
            AstraConsole.print(MOVE_UP_CLEAR);
        }
    }

    private void cleanupOutput(Optional<String> result) {
        if (req.echoOff() && result.isPresent() && AstraColors.enabled()) {
            AstraConsole.print(MOVE_UP_CLEAR);
            AstraConsole.println("@!>!@ " + req.displayContentWhenDone().apply(result.get()));
        }
        else if (result.isEmpty() && req.defaultOption().isPresent()) {
            AstraConsole.print(MOVE_UP_CLEAR);
            AstraConsole.println("@!>!@ " + req.defaultOption().get());
        }
        AstraConsole.println();
    }
}
