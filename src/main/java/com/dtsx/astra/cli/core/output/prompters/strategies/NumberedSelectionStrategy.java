package com.dtsx.astra.cli.core.output.prompters.strategies;

import com.dtsx.astra.cli.core.CliEnvironment;
import com.dtsx.astra.cli.core.exceptions.internal.cli.ExecutionCancelledException;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.core.output.prompters.PromptRequest;
import com.dtsx.astra.cli.core.output.prompters.SelectionStrategy;
import lombok.val;

import java.io.Console;
import java.util.Objects;
import java.util.Optional;

import static com.dtsx.astra.cli.utils.StringUtils.isInteger;
import static com.dtsx.astra.cli.utils.StringUtils.truncate;

public class NumberedSelectionStrategy<T> implements SelectionStrategy<T> {
    private static final int PAGE_SIZE = 10;
    private static final String MOVE_UP_CLEAR = "\033[1A\033[2K\r";
    private static final String CLEAR_MOVE_UP = "\033[2K\033[1A\r\033[2C";

    private final PromptRequest.Closed<T> req;
    private final Console console;

    private int sliceStart = 0;

    public NumberedSelectionStrategy(PromptRequest.Closed<T> req) {
        this.req = req;
        this.console = Objects.requireNonNull(AstraConsole.getConsole());
    }

    public static class Meta implements SelectionStrategy.Meta.Closed {
        @Override
        public boolean isSupported() {
            return OutputType.isHuman() && CliEnvironment.isTty();
        }

        @Override
        public <T> SelectionStrategy<T> mkInstance(PromptRequest.Closed<T> request) {
            return new NumberedSelectionStrategy<>(request);
        }
    }

    @Override
    public Optional<T> select() {
        drawPrompt();
        drawOptions();
        drawFooter();

        val result = handleInput();

        if (req.clearAfterSelection()) {
            clearPrompt();
            clearOptionsAndFooter();
        } else {
            AstraConsole.println();
        }

        return result;
    }

    private void drawPrompt() {
        AstraConsole.println(req.prompt());
    }

    private void drawOptions() {
        for (var i = sliceStart; i < sliceStart + PAGE_SIZE && i < req.options().size(); i++) {
            AstraConsole.printf("@!%d)!@ %s%n", i + 1, req.options().get(i));
        }
    }

    private void drawFooter() {
        AstraConsole.printf("%n");

        if (paginationEnabled()) {
            AstraConsole.printf("[@!n!@]ext page, [@!p!@]revious page, [@!1-%d!@] to select (showing @!%d-%d!@):%n", req.options().size(), sliceStart + 1, sliceEnd());
        } else {
            AstraConsole.printf("[@!1-%d!@] to select:%n", req.options().size());
        }

        AstraConsole.printf("@!> !@");
    }

    private Optional<T> handleInput() {
        while (true) {
            val input = console.readLine().trim();

            if ("n".equals(input)) {
                handleNextPage();
            }
            else if ("p".equals(input)) {
                handlePreviousPage();
            }
            else if (input.isEmpty()) {
                AstraConsole.print(CLEAR_MOVE_UP);
            }
            else if (isInteger(input)) {
                return Optional.of(req.mapper().apply(req.options().get(Integer.parseInt(input) - 1)));
            }
            else if ("q".equals(input)) {
                throw new ExecutionCancelledException();
            }
            else {
                throw new ExecutionCancelledException("@|bold,red Execution cancelled due to invalid input: '" + truncate(input, 10) + "'|@");
            }
        }
    }

    private void handleNextPage() {
        clearOptionsAndFooter();

        sliceStart = (req.options().size() > (sliceStart + PAGE_SIZE))
            ? sliceStart + PAGE_SIZE
            : 0;

        drawOptions();
        drawFooter();
    }

    private void handlePreviousPage() {
        clearOptionsAndFooter();

        int total = req.options().size();
        int remainder = total % PAGE_SIZE;

        sliceStart = (sliceStart - PAGE_SIZE < 0)
            ? (remainder == 0) ? total - PAGE_SIZE : total - remainder
            : sliceStart - PAGE_SIZE;

        drawOptions();
        drawFooter();
    }

    private void clearOptionsAndFooter() {
        for (int i = 0; i < (sliceEnd() - sliceStart) + 3; i++) {
            AstraConsole.print(MOVE_UP_CLEAR);
        }
    }

    private void clearPrompt() {
        for (int i = 0; i < req.prompt().split("\n").length + 1; i++) {
            AstraConsole.print(MOVE_UP_CLEAR);
        }
    }

    private int sliceEnd() {
        return Math.min((sliceStart + PAGE_SIZE), req.options().size());
    }

    private boolean paginationEnabled() {
        return req.options().size() > PAGE_SIZE;
    }
}
