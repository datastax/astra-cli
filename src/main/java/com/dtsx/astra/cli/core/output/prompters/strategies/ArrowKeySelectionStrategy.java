package com.dtsx.astra.cli.core.output.prompters.strategies;

import com.dtsx.astra.cli.core.CliEnvironment;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.ExecutionCancelledException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.core.output.prompters.PromptRequest;
import com.dtsx.astra.cli.core.output.prompters.SelectionStrategy;
import lombok.SneakyThrows;
import lombok.val;

import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.core.CliEnvironment.isWindows;

public class ArrowKeySelectionStrategy<T> implements SelectionStrategy<T> {
    private static final int ESC = 27;
    private static final int BRACKET = 91;
    private static final int ARROW_UP = 65;
    private static final int ARROW_DOWN = 66;
    private static final int ENTER = 13;
    private static final int LINE_FEED = 10;
    private static final int CTRL_C = 3;
    private static final int BACKSPACE = 127;
    private static final int CTRL_H = 8;
    private static final int PRINTABLE_START = 32;
    private static final int PRINTABLE_END = 126;
    
    private static final String HIDE_CURSOR = "\033[?25l";
    private static final String SHOW_CURSOR = "\033[?25h";
    private static final String MOVE_UP_CLEAR = "\033[1A\033[2K\r";
    private static final String UNDERLINE_START = "\033[4m";
    private static final String UNDERLINE_END = "\033[24m";

    private final PromptRequest.Closed<T> req;
    
    private String filterText = "";
    private volatile boolean rawModeEnabled;

    private final String fixedPrompt;
    private List<String> filteredOptions;
    private int selectedIndex;

    public ArrowKeySelectionStrategy(PromptRequest.Closed<T> req) {
        this.req = req;
        this.fixedPrompt = req.prompt().replace("\n", "\r\n");
        this.filteredOptions = List.copyOf(req.options());
        this.selectedIndex = Math.max(req.defaultOption().map(filteredOptions::indexOf).orElse(0), 0);
    }
    
    public static class Meta implements SelectionStrategy.Meta.Closed {
        @Override
        public boolean isSupported() {
            return OutputType.isHuman() && CliEnvironment.isTty() && !isWindows() && AstraColors.enabled();
        }
        
        @Override
        public <T> SelectionStrategy<T> mkInstance(PromptRequest.Closed<T> request) {
            return new ArrowKeySelectionStrategy<>(request);
        }
    }
    
    @Override
    public Optional<T> select() {
        val shutdownHook = new Thread(this::disableRawMode);

        try {
            setup(shutdownHook);
            
            showPromptAndStartSelector();

            val result = handleInput();

            if (req.clearAfterSelection()) {
                clearPrompt();
            }

            return result;
        } finally {
            cleanup(shutdownHook);
        }
    }

    private void showPromptAndStartSelector() {
        AstraConsole.println(fixedPrompt);
        AstraConsole.print(HIDE_CURSOR);
        redraw();
    }
    
    private void redraw() {
        if (filteredOptions.isEmpty()) {
            AstraConsole.printf("\r%s%s%n", AstraColors.NEUTRAL_400.use("> Oops! No matches found for "), formatFilter(filterText));
            AstraConsole.printf("%n\r@!Esc!@ to cancel, @!Backspace!@ to remove filter%n");
        } else {
            val defaultIndex = req.defaultOption().map(filteredOptions::indexOf);

            for (int i = 0; i < filteredOptions.size(); i++) {
                drawOption(i, defaultIndex.isPresent() && defaultIndex.get() == i);
            }
            AstraConsole.printf("%n\r@!↑↓!@ to navigate, @!Enter!@ to select, @!type!@ to filter%n");
        }
    }
    
    private void drawOption(int index, boolean isDefault) {
        val isSelected = index == selectedIndex;
        val prefix = isSelected ? AstraColors.BLUE_300.use(">") : " ";
        val option = filteredOptions.get(index);

        val optionStr = (isSelected)
            ? highlight(highlightMatch(option))
            : highlightMatch(option);

        val isDefaultStr = (isDefault && req.labelDefault())
            ? AstraColors.PURPLE_300.use(" (default)")
            : "";

        AstraConsole.printf("\r%s %s%s%n", prefix, optionStr, isDefaultStr);
    }
    
    private String highlightMatch(String option) {
        if (filterText.isEmpty()) {
            return option;
        }
        
        val matchIndex = option.toLowerCase().indexOf(filterText.toLowerCase());
        if (matchIndex == -1) {
            return option;
        }
        
        val before = option.substring(0, matchIndex);
        val match = option.substring(matchIndex, matchIndex + filterText.length());
        val after = option.substring(matchIndex + filterText.length());
        
        return before + UNDERLINE_START + match + UNDERLINE_END + after;
    }
    
    private String formatFilter(String filter) {
        if (AstraColors.enabled()) {
            return AstraColors.NEUTRAL_400.use(UNDERLINE_START + filter + UNDERLINE_END);
        } else {
            return '"' + filter + '"';
        }
    }

    private void updateFilter() {
        clearSelector();

        filteredOptions = req.options().stream()
            .filter(option -> option.toLowerCase().contains(filterText.toLowerCase()))
            .toList();
        
        selectedIndex = filteredOptions.isEmpty() || selectedIndex >= filteredOptions.size() ? 0 : selectedIndex;

        redraw();
    }

    @SneakyThrows
    private Optional<T> handleInput() {
        while (true) {
            val input = System.in.read();
            
            if (isEscapeKey(input)) {
                val result = handleEscapeSequence();

                if (result.isPresent()) {
                    return result.get();
                }
            }
            else if (isSelectionKey(input)) {
                val res = handleSelection();

                if (res.isPresent()) {
                    return res;
                }
            }
            else if (isQuitKey(input)) {
                return handleQuit();
            }
            else if (isBackspaceKey(input)) {
                handleBackspace();
            }
            else if (isPrintable(input)) {
                handleTyping(input);
            }
        }
    }

    @SneakyThrows
    private Optional<Optional<T>> handleEscapeSequence() {
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

    private Optional<T> handleSelection() {
        if (!filteredOptions.isEmpty()) {
            val selected = filteredOptions.get(selectedIndex);

            clearSelector();

            if (!req.clearAfterSelection()) {
                AstraConsole.printf("@!> %s!@\r\n\r\n", selected);
            }
            
            return Optional.of(req.mapper().apply(selected));
        }
        return Optional.empty();
    }
    
    private Optional<T> handleQuit() {
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
    
    private void moveSelectionUp() {
        if (!filteredOptions.isEmpty()) {
            selectedIndex = (selectedIndex - 1 + filteredOptions.size()) % filteredOptions.size();
            clearSelector();
            redraw();
        }
    }
    
    private void moveSelectionDown() {
        if (!filteredOptions.isEmpty()) {
            selectedIndex = (selectedIndex + 1) % filteredOptions.size();
            clearSelector();
            redraw();
        }
    }

    private void clearSelector() {
        val lastDrawnLines = Math.max(filteredOptions.size(), 1) + 2;

        for (int i = 0; i < lastDrawnLines; i++) {
            AstraConsole.print(MOVE_UP_CLEAR);
        }
    }
    
    private void clearPrompt() {
        for (int i = 0; i < fixedPrompt.split("\n").length; i++) {
            AstraConsole.print(MOVE_UP_CLEAR);
        }
    }
    
    private boolean isEscapeKey(int input) {
        return input == ESC;
    }

    private boolean isSelectionKey(int input) {
        return input == ENTER || input == LINE_FEED;
    }
    
    private boolean isQuitKey(int input) {
        return input == CTRL_C;
    }
    
    private boolean isBackspaceKey(int input) {
        return input == BACKSPACE || input == CTRL_H;
    }
    
    private boolean isPrintable(int input) {
        return input >= PRINTABLE_START && input <= PRINTABLE_END;
    }

    private void setup(Thread shutdownHook) {
        try {
            Runtime.getRuntime().addShutdownHook(shutdownHook);

            val exitCode = new ProcessBuilder("/bin/sh", "-c", "stty raw -echo < /dev/tty").start().waitFor();

            if (exitCode == 0) {
                rawModeEnabled = true;
            } else {
                throw new CongratsYouFoundABugException("TODO1");
            }
        } catch (Exception ignored) {
            throw new CongratsYouFoundABugException("TODO2");
        }
    }

    private void cleanup(Thread shutdownHook) {
        AstraConsole.print(SHOW_CURSOR);
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
        disableRawMode();
    }
    
    private void disableRawMode() {
        if (!rawModeEnabled) return;
        
        try {
            new ProcessBuilder("/bin/sh", "-c", "stty cooked echo < /dev/tty").start().waitFor();
        } catch (Exception ignored) {
            throw new CongratsYouFoundABugException("");
        }
    }
}
