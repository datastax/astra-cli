package com.dtsx.astra.cli.core.output.select;

import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.output.OutputType;
import lombok.SneakyThrows;
import lombok.val;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.utils.MiscUtils.isWindows;

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

    private static final int NO_MATCHES_LINES = 3;
    private static final int HELP_LINES = 2;
    
    private final String prompt;
    private final NEList<String> options;
    private final Function<String, T> mapper;
    
    private int selectedIndex;
    private volatile boolean rawModeEnabled;
    private String filterText;
    private List<String> filteredOptions;
    private int lastDrawnLines;
    private int totalLinesDrawn;
    
    public ArrowKeySelectionStrategy(String prompt, NEList<String> options, Optional<String> defaultOption, Function<String, T> mapper) {
        this.prompt = prompt;
        this.options = options;
        this.mapper = mapper;
        this.filterText = "";
        this.filteredOptions = List.copyOf(options);
        this.selectedIndex = Math.max(defaultOption.map(filteredOptions::indexOf).orElse(0), 0);
        this.lastDrawnLines = 0;
        this.totalLinesDrawn = 0;
        this.rawModeEnabled = false;
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanupOnShutdown));
    }
    
    public static class Meta implements SelectionStrategy.Meta {
        @Override
        public boolean isSupported() {
            return System.console() != null && OutputType.isHuman() && AstraConsole.isTty() && !isWindows();
        }
        
        @Override
        public <T> SelectionStrategy<T> mkInstance(String prompt, NEList<String> options, Optional<String> defaultOption, Function<String, T> mapper) {
            return new ArrowKeySelectionStrategy<>(prompt, options, defaultOption, mapper);
        }
    }

    @Override
    public SelectStatus<T> select() {
        return select(false);
    }
    
    @Override
    public SelectStatus<T> select(boolean clearAfterSelection) {
        try {
            enableRawMode();

            if (!rawModeEnabled) {
                throw new CongratsYouFoundABugException("");
            }
            
            showPromptAndStartSelector();

            val result = handleInput(clearAfterSelection);
            
            if (clearAfterSelection) {
                clearAll();
            }
            
            return result;
        } finally {
            cleanup();
        }
    }

    private void showPromptAndStartSelector() {
        AstraConsole.println(prompt);
        totalLinesDrawn = 1; // Count the prompt line
        AstraConsole.print(HIDE_CURSOR);
        redraw();
    }
    
    private void cleanup() {
        AstraConsole.print(SHOW_CURSOR);
        disableRawMode();
    }
    
    private void cleanupOnShutdown() {
        if (rawModeEnabled) {
            disableRawMode();
        }
    }
    
    private void redraw() {
        if (filteredOptions.isEmpty()) {
            drawNoMatches();
        } else {
            drawOptions();
        }
    }
    
    private void drawNoMatches() {
        AstraConsole.printf("\r%s%s%n", AstraColors.NEUTRAL_400.use("Oops! No matches found for "), formatFilter(filterText));
        AstraConsole.printf("\r%n\r@!Esc!@ to cancel, @!Backspace!@ to remove filter%n");
        lastDrawnLines = NO_MATCHES_LINES;
        totalLinesDrawn += lastDrawnLines;
    }
    
    private void drawOptions() {
        for (int i = 0; i < filteredOptions.size(); i++) {
            drawOption(i);
        }

        AstraConsole.printf("\r%n\r@!↑↓!@ to navigate, @!Enter!@ to select, @!type!@ to filter%n");
        lastDrawnLines = filteredOptions.size() + HELP_LINES;
        totalLinesDrawn += lastDrawnLines;
    }
    
    private void drawOption(int index) {
        val isSelected = index == selectedIndex;
        val prefix = isSelected ? AstraColors.BLUE_300.use(">") : " ";
        val option = filteredOptions.get(index);
        val displayOption = highlightMatch(option);
        
        if (isSelected) {
            AstraConsole.printf("\r%s %s%n", prefix, highlight(displayOption));
        } else {
            AstraConsole.printf("\r%s %s%n", prefix, displayOption);
        }
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
        filteredOptions = options.stream()
            .filter(option -> option.toLowerCase().contains(filterText.toLowerCase()))
            .toList();
        
        selectedIndex = filteredOptions.isEmpty() || selectedIndex >= filteredOptions.size() ? 0 : selectedIndex;
    }
    
    private void clearAndRedraw() {
        for (int i = 0; i < lastDrawnLines; i++) {
            AstraConsole.print(MOVE_UP_CLEAR);
        }
        redraw();
    }

    @SneakyThrows
    private SelectStatus<T> handleInput(boolean clearAfterSelection) {
        while (true) {
            val input = System.in.read();
            
            if (isEscapeKey(input)) {
                val result = handleEscapeSequence();

                if (result.isPresent()) {
                    return result.get();
                }
            }
            else if (isSelectionKey(input)) {
                return handleSelection(clearAfterSelection);
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
    private Optional<SelectStatus<T>> handleEscapeSequence() {
        if (System.in.available() <= 0) {
            clearSelector();
            return Optional.of(new SelectStatus.NoAnswer<>());
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
    
    private SelectStatus<T> handleSelection(boolean clearAfterSelection) {
        if (!filteredOptions.isEmpty()) {
            clearSelector();
            val selected = filteredOptions.get(selectedIndex);
            if (!clearAfterSelection) {
                AstraConsole.printf("Selected: %s%n", highlight(selected));
            }
            return new SelectStatus.Selected<>(mapper.apply(selected));
        }
        return new SelectStatus.NoAnswer<>();
    }
    
    private SelectStatus<T> handleQuit() {
        clearSelector();
        return new SelectStatus.NoAnswer<>();
    }
    
    private void handleBackspace() {
        if (!filterText.isEmpty()) {
            filterText = filterText.substring(0, filterText.length() - 1);
            updateFilter();
            clearAndRedraw();
        }
    }
    
    private void handleTyping(int input) {
        filterText += (char) input;
        updateFilter();
        clearAndRedraw();
    }
    
    private void moveSelectionUp() {
        if (!filteredOptions.isEmpty()) {
            selectedIndex = (selectedIndex - 1 + filteredOptions.size()) % filteredOptions.size();
            clearAndRedraw();
        }
    }
    
    private void moveSelectionDown() {
        if (!filteredOptions.isEmpty()) {
            selectedIndex = (selectedIndex + 1) % filteredOptions.size();
            clearAndRedraw();
        }
    }
    
    private void clearSelector() {
        for (int i = 0; i < lastDrawnLines; i++) {
            AstraConsole.print(MOVE_UP_CLEAR);
        }
    }
    
    private void clearAll() {
        for (int i = 0; i < totalLinesDrawn; i++) {
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

    private void enableRawMode() {
        if (rawModeEnabled) return;
        
        try {
            if (new ProcessBuilder("/bin/sh", "-c", "stty raw -echo < /dev/tty").start().waitFor() == 0) {
                rawModeEnabled = true;
            }
        } catch (IOException | InterruptedException ignored) {
        }
    }
    
    private void disableRawMode() {
        if (!rawModeEnabled) return;
        
        try {
            new ProcessBuilder("/bin/sh", "-c", "stty cooked echo < /dev/tty").start().waitFor();
            rawModeEnabled = false;
        } catch (IOException | InterruptedException ignored) {
            rawModeEnabled = false;
        }
    }
}
