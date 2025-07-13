package com.dtsx.astra.cli.core.output.select;

import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.output.OutputType;
import lombok.val;

import java.io.Console;
import java.util.Optional;
import java.util.function.Function;

public class NumberedSelectionStrategy<T> implements SelectionStrategy<T> {
    private final Console console;
    private final String prompt;
    private final NEList<String> options;
    private final Optional<String> defaultOption;
    private final Function<String, T> mapper;
    private int totalLinesDrawn;
    
    public NumberedSelectionStrategy(String prompt, NEList<String> options, Optional<String> defaultOption, Function<String, T> mapper) {
        this.console = System.console();
        this.prompt = prompt;
        this.options = options;
        this.defaultOption = defaultOption;
        this.mapper = mapper;
        this.totalLinesDrawn = 0;
    }
    
    public static class Meta implements SelectionStrategy.Meta {
        @Override
        public boolean isSupported() {
            return System.console() != null && OutputType.isHuman();
        }
        
        @Override
        public <T> SelectionStrategy<T> mkInstance(String prompt, NEList<String> options, Optional<String> defaultOption, Function<String, T> mapper) {
            return new NumberedSelectionStrategy<>(prompt, options, defaultOption, mapper);
        }
    }
    
    @Override
    public SelectStatus<T> select() {
        return select(false);
    }
    
    @Override
    public SelectStatus<T> select(boolean clearAfterSelection) {
        return performSelection(clearAfterSelection);
    }
    
    private SelectStatus<T> performSelection(boolean clearAfterSelection) {
        console.printf("%s%n", AstraConsole.format(this.prompt));
        totalLinesDrawn = 1; // Count the prompt line
        
        for (int i = 0; i < this.options.size(); i++) {
            val isDefault = this.defaultOption.isPresent() && this.options.get(i).equals(this.defaultOption.get());
            val marker = isDefault ? AstraConsole.format(" @!(default)!@") : "";
            console.printf("  %s) %s%s%n", 
                AstraConsole.format("@!" + (i + 1) + "!@"), 
                this.options.get(i), 
                marker);
            totalLinesDrawn++;
        }
        
        val defaultText = this.defaultOption.map(def -> " (default: " + def + ")").orElse("");
        console.printf("%n%s (1-%d)%s: ", 
            AstraConsole.format("@!Enter!@ selection"), 
            this.options.size(), 
            defaultText);
        totalLinesDrawn += 2; // Empty line + input prompt
        
        val input = console.readLine();
        
        if (input == null || input.trim().isEmpty()) {
            return this.defaultOption
                .<SelectStatus<T>>map(def -> new SelectStatus.Default<>(this.mapper.apply(def)))
                .orElse(new SelectStatus.NoAnswer<>());
        }
        
        try {
            val choice = Integer.parseInt(input.trim());
            if (choice >= 1 && choice <= this.options.size()) {
                val selected = this.options.get(choice - 1);
                if (clearAfterSelection) {
                    clearAll();
                } else {
                    console.printf("Selected: %s%n", AstraConsole.format("@!" + selected + "!@"));
                }
                return new SelectStatus.Selected<>(this.mapper.apply(selected));
            }
        } catch (NumberFormatException ignored) {
        }
        
        if (!clearAfterSelection) {
            console.printf("Invalid selection.%n");
        } else {
            clearAll();
        }

        return this.defaultOption.map(def -> new SelectStatus.Default<>(this.mapper.apply(def)))
            .orElseThrow();
    }
    
    private void clearAll() {
        for (int i = 0; i < totalLinesDrawn; i++) {
            console.printf("\033[1A\033[2K\r");
        }
    }
}
