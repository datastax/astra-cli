package com.dtsx.astra.cli.core.output.prompters.strategies;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.internal.cli.ExecutionCancelledException;
import com.dtsx.astra.cli.core.output.prompters.PromptRequest;
import com.dtsx.astra.cli.core.output.prompters.SelectionStrategy;
import lombok.val;

import java.io.Console;
import java.util.Objects;
import java.util.Optional;

import static com.dtsx.astra.cli.utils.StringUtils.isPositiveInteger;
import static com.dtsx.astra.cli.utils.StringUtils.truncate;

public class NumberedSelectionStrategy<T> implements SelectionStrategy<T> {
    private static final int PAGE_SIZE = 10;
    private static final String MOVE_UP_CLEAR = "\033[1A\033[2K\r";
    private static final String CLEAR_MOVE_UP = "\033[2K\033[1A\r\033[2C";

    private final CliContext ctx;
    private final PromptRequest.Closed<T> req;
    private final Console console;

    private int sliceStart = 0;

    public NumberedSelectionStrategy(CliContext ctx, PromptRequest.Closed<T> req) {
        this.ctx = ctx;
        this.req = req;
        this.console = Objects.requireNonNull(ctx.console().getConsole());
    }

    public static class Meta implements SelectionStrategy.Meta.Closed {
        @Override
        public boolean isSupported(CliContext ctx) {
            return ctx.outputIsHuman() && ctx.isTty();
        }

        @Override
        public <T> SelectionStrategy<T> mkInstance(CliContext ctx, PromptRequest.Closed<T> request) {
            return new NumberedSelectionStrategy<>(ctx, request);
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
            ctx.console().println();
        }

        return result;
    }

    private void drawPrompt() {
        ctx.console().println(req.prompt());
    }

    private void drawOptions() {
        for (var i = sliceStart; i < sliceStart + PAGE_SIZE && i < req.options().size(); i++) {
            ctx.console().printf("@!%d)!@ %s%n", i + 1, req.options().get(i));
        }
    }

    private void drawFooter() {
        ctx.console().printf("%n");

        if (paginationEnabled()) {
            ctx.console().printf("[@!n!@]ext page, [@!p!@]revious page, [@!1-%d!@] to select (showing @!%d-%d!@):%n", req.options().size(), sliceStart + 1, sliceEnd());
        } else {
            ctx.console().printf("[@!1-%d!@] to select:%n", req.options().size());
        }

        ctx.console().printf("@!> !@");
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
                ctx.console().print(CLEAR_MOVE_UP);
            }
            else if (isPositiveInteger(input)) {
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
            ctx.console().print(MOVE_UP_CLEAR);
        }
    }

    private void clearPrompt() {
        for (int i = 0; i < req.prompt().split("\n").length + 1; i++) {
            ctx.console().print(MOVE_UP_CLEAR);
        }
    }

    private int sliceEnd() {
        return Math.min((sliceStart + PAGE_SIZE), req.options().size());
    }

    private boolean paginationEnabled() {
        return req.options().size() > PAGE_SIZE;
    }
}
