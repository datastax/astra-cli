package com.dtsx.astra.cli.core.output.prompters.strategies;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.ExecutionCancelledException;
import com.dtsx.astra.cli.core.output.prompters.SelectionStrategy;
import lombok.SneakyThrows;
import lombok.val;

import java.util.Optional;

abstract class AbstractTtySelectionStrategy<R> implements SelectionStrategy<R> {
    protected static final int ESC            = 27;
    protected static final int BRACKET        = 91;
    protected static final int ARROW_UP       = 65;
    protected static final int ARROW_DOWN     = 66;
    protected static final int ENTER          = 13;
    protected static final int LINE_FEED      = 10;
    protected static final int CTRL_C         = 3;
    protected static final int BACKSPACE      = 127;
    protected static final int CTRL_H         = 8;
    protected static final int PRINTABLE_START = 32;
    protected static final int PRINTABLE_END  = 126;

    protected static final String HIDE_CURSOR    = "\033[?25l";
    protected static final String SHOW_CURSOR    = "\033[?25h";
    protected static final String MOVE_UP_CLEAR  = "\033[1A\033[2K\r";
    protected static final String UNDERLINE_START = "\033[4m";
    protected static final String UNDERLINE_END   = "\033[24m";

    protected final CliContext ctx;
    protected final String fixedPrompt;

    protected String filterText = "";
    protected int selectedIndex = 0;

    private volatile boolean rawModeEnabled;

    protected AbstractTtySelectionStrategy(CliContext ctx, String rawPrompt) {
        this.ctx = ctx;
        this.fixedPrompt = rawPrompt.replace("\n", "\r\n");
    }

    @Override
    public final Optional<R> select() {
        val shutdownHook = new Thread(this::disableRawMode);

        try {
            setup(shutdownHook);
            showPromptAndStartSelector();

            val result = handleInput();

            if (shouldClearAfterSelection()) {
                clearPrompt();
            }

            return result;
        } finally {
            cleanup(shutdownHook);
        }
    }

    protected abstract void redraw();

    protected abstract int filteredItemCount();

    protected abstract Optional<R> handleSelection();

    protected abstract boolean shouldClearAfterSelection();

    protected abstract void updateFilter();

    protected void handleExtraKeys(int input) {}

    @SneakyThrows
    private Optional<R> handleInput() {
        while (true) {
            val input = System.in.read();

            if (isEscapeKey(input)) {
                val result = handleEscapeSequence();
                if (result.isPresent()) {
                    return result.get();
                }
            } else if (isSelectionKey(input)) {
                val res = handleSelection();
                if (res.isPresent()) {
                    return res;
                }
            } else if (isQuitKey(input)) {
                return handleQuit();
            } else if (isBackspaceKey(input)) {
                handleBackspace();
            } else if (isPrintable(input)) {
                handleTyping(input);
            } else {
                handleExtraKeys(input);
            }
        }
    }

    @SneakyThrows
    private Optional<Optional<R>> handleEscapeSequence() {
        if (System.in.available() <= 0) {
            clearSelector();
            return Optional.of(Optional.empty());
        }

        val next1 = System.in.read();

        if (next1 == BRACKET) {
            val next2 = System.in.read();

            if (next2 == ARROW_UP) {
                moveSelectionUp();
            } else if (next2 == ARROW_DOWN) {
                moveSelectionDown();
            }
        }

        return Optional.empty();
    }

    private Optional<R> handleQuit() {
        clearSelector();
        throw new ExecutionCancelledException();
    }

    private void handleBackspace() {
        if (!filterText.isEmpty()) {
            filterText = filterText.substring(0, filterText.length() - 1);
            updateFilter();
        }
    }

    private void handleTyping(int input) {
        filterText += (char) input;
        updateFilter();
    }

    protected void moveSelectionUp() {
        val count = filteredItemCount();
        if (count > 0) {
            selectedIndex = (selectedIndex - 1 + count) % count;
            clearSelector();
            redraw();
        }
    }

    protected void moveSelectionDown() {
        val count = filteredItemCount();
        if (count > 0) {
            selectedIndex = (selectedIndex + 1) % count;
            clearSelector();
            redraw();
        }
    }

    protected void showPromptAndStartSelector() {
        ctx.console().errorln(fixedPrompt);
        ctx.console().error(HIDE_CURSOR);
        redraw();
    }

    protected void clearSelector() {
        val lastDrawnLines = Math.max(filteredItemCount(), 1) + 2;
        for (int i = 0; i < lastDrawnLines; i++) {
            ctx.console().error(MOVE_UP_CLEAR);
        }
    }

    protected void clearPrompt() {
        for (int i = 0; i < fixedPrompt.split("\n").length; i++) {
            ctx.console().error(MOVE_UP_CLEAR);
        }
    }

    protected String highlightMatch(String option) {
        if (filterText.isEmpty()) {
            return option;
        }

        val matchIndex = option.toLowerCase().indexOf(filterText.toLowerCase());
        if (matchIndex == -1) {
            return option;
        }

        val before = option.substring(0, matchIndex);
        val match  = option.substring(matchIndex, matchIndex + filterText.length());
        val after  = option.substring(matchIndex + filterText.length());

        return before + UNDERLINE_START + match + UNDERLINE_END + after;
    }

    protected String formatFilter(String filter) {
        if (ctx.ansiEnabled()) {
            return ctx.colors().NEUTRAL_400.use(UNDERLINE_START + filter + UNDERLINE_END);
        } else {
            return '"' + filter + '"';
        }
    }

    protected boolean isEscapeKey(int input) {
        return input == ESC;
    }

    protected boolean isSelectionKey(int input) {
        return input == ENTER || input == LINE_FEED;
    }

    protected boolean isQuitKey(int input) {
        return input == CTRL_C;
    }

    protected boolean isBackspaceKey(int input) {
        return input == BACKSPACE || input == CTRL_H;
    }

    protected boolean isPrintable(int input) {
        return input >= PRINTABLE_START && input <= PRINTABLE_END;
    }

    private void setup(Thread shutdownHook) {
        try {
            Runtime.getRuntime().addShutdownHook(shutdownHook);

            val exitCode = new ProcessBuilder("/bin/sh", "-c", "stty raw -echo < /dev/tty").start().waitFor();

            if (exitCode == 0) {
                rawModeEnabled = true;
            } else {
                throw new CongratsYouFoundABugException("Something went wrong enabling raw mode (got exit code " + exitCode + ")");
            }
        } catch (Exception e) {
            throw new CongratsYouFoundABugException("Something went wrong enabling raw mode: '" + e.getMessage() + "'");
        }
    }

    private void cleanup(Thread shutdownHook) {
        ctx.console().error(SHOW_CURSOR);
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
        disableRawMode();
    }

    private void disableRawMode() {
        if (!rawModeEnabled) return;

        try {
            new ProcessBuilder("/bin/sh", "-c", "stty cooked echo < /dev/tty").start().waitFor();
        } catch (Exception e) {
            throw new CongratsYouFoundABugException("Something went wrong disabling raw mode: '" + e.getMessage() + "'");
        }
    }
}
