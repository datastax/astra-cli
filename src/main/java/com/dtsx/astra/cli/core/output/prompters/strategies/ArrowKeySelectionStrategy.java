package com.dtsx.astra.cli.core.output.prompters.strategies;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.output.prompters.PromptRequest;
import com.dtsx.astra.cli.core.output.prompters.SelectionStrategy;
import lombok.val;

import java.util.List;
import java.util.Optional;

public class ArrowKeySelectionStrategy<T> extends AbstractTtySelectionStrategy<T> {
    private final PromptRequest.Closed<T> req;

    private List<String> filteredOptions;

    public ArrowKeySelectionStrategy(CliContext ctx, PromptRequest.Closed<T> req) {
        super(ctx, req.prompt());
        this.req = req;
        this.filteredOptions = List.copyOf(req.options());
        this.selectedIndex = Math.max(req.defaultOption().map(filteredOptions::indexOf).orElse(0), 0);
    }

    public static class Meta implements SelectionStrategy.Meta.Closed {
        @Override
        public boolean isSupported(CliContext ctx) {
            return ctx.isTty() && ctx.isNotWindows() && ctx.ansiEnabled();
        }

        @Override
        public <T> SelectionStrategy<T> mkInstance(CliContext ctx, PromptRequest.Closed<T> request) {
            return new ArrowKeySelectionStrategy<>(ctx, request);
        }
    }

    @Override
    protected int filteredItemCount() {
        return filteredOptions.size();
    }

    @Override
    protected boolean shouldClearAfterSelection() {
        return req.clearAfterSelection();
    }

    @Override
    protected void updateFilter() {
        clearSelector();

        filteredOptions = req.options().stream()
            .filter(option -> option.toLowerCase().contains(filterText.toLowerCase()))
            .toList();

        selectedIndex = filteredOptions.isEmpty() || selectedIndex >= filteredOptions.size() ? 0 : selectedIndex;

        redraw();
    }

    @Override
    protected void redraw() {
        if (filteredOptions.isEmpty()) {
            ctx.console().errorf("\r%s%s%n", ctx.colors().NEUTRAL_400.use("> Oops! No matches found for "), formatFilter(filterText));
            ctx.console().errorf("%n\r@!Esc!@ to cancel, @!Backspace!@ to remove filter%n");
        } else {
            val defaultIndex = req.defaultOption().map(filteredOptions::indexOf);

            for (int i = 0; i < filteredOptions.size(); i++) {
                drawOption(i, defaultIndex.isPresent() && defaultIndex.get() == i);
            }
            ctx.console().errorf("%n\r@!↑↓!@ to navigate, @!Enter!@ to select, @!type!@ to filter%n");
        }
    }

    @Override
    protected Optional<T> handleSelection() {
        if (!filteredOptions.isEmpty()) {
            val selected = filteredOptions.get(selectedIndex);

            clearSelector();

            if (!req.clearAfterSelection()) {
                ctx.console().errorf("@!> %s!@\r\n\r\n", selected);
            }

            return Optional.of(req.mapper().apply(selected));
        }
        return Optional.empty();
    }

    private void drawOption(int index, boolean isDefault) {
        val isSelected = index == selectedIndex;
        val prefix = isSelected ? ctx.colors().BLUE_300.use(">") : " ";
        val option = filteredOptions.get(index);

        val optionStr = isSelected
            ? ctx.highlight(highlightMatch(option), false)
            : highlightMatch(option);

        val isDefaultStr = (isDefault && req.labelDefault())
            ? ctx.colors().PURPLE_300.use(" (default)")
            : "";

        ctx.console().errorf("\r%s %s%s%n", prefix, optionStr, isDefaultStr);
    }
}
