package com.dtsx.astra.cli.core.output.prompters.strategies;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.output.prompters.PromptRequest;
import com.dtsx.astra.cli.core.output.prompters.SelectionStrategy;
import lombok.val;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CheckboxSelectionStrategy<T> extends AbstractTtySelectionStrategy<List<T>> {
    private static final int SPACE  = 32;
    private static final int CTRL_A = 1;

    private final PromptRequest.MultiClosed<T> req;

    private final Set<String> checkedOptions = new HashSet<>();

    private record RenderItem(String text, String optionValue) {}

    private final List<RenderItem> allItems = new ArrayList<>();
    private List<RenderItem> filteredItems  = new ArrayList<>();

    public CheckboxSelectionStrategy(CliContext ctx, PromptRequest.MultiClosed<T> req) {
        super(ctx, req.prompt());
        this.req = req;
        if (req.defaultOptions() != null) {
            this.checkedOptions.addAll(req.defaultOptions());
        }

        buildAllItems();
        this.filteredItems = new ArrayList<>(allItems);
    }

    public static class Meta implements SelectionStrategy.Meta.MultiClosed {
        @Override
        public boolean isSupported(CliContext ctx) {
            return ctx.isTty() && ctx.isNotWindows() && ctx.ansiEnabled();
        }

        @Override
        public <T> SelectionStrategy<List<T>> mkInstance(CliContext ctx, PromptRequest.MultiClosed<T> request) {
            return new CheckboxSelectionStrategy<>(ctx, request);
        }
    }

    @Override
    protected int filteredItemCount() {
        return filteredItems.size();
    }

    @Override
    protected boolean shouldClearAfterSelection() {
        return req.clearAfterSelection();
    }

    @Override
    protected boolean isPrintable(int input) {
        return input > PRINTABLE_START && input <= PRINTABLE_END;
    }

    @Override
    protected void handleExtraKeys(int input) {
        if (input == SPACE) {
            handleSpace();
        } else if (input == CTRL_A) {
            handleSelectAll();
        }
    }

    @Override
    protected void updateFilter() {
        clearSelector();

        if (filterText.isEmpty()) {
            filteredItems = new ArrayList<>(allItems);
        } else {
            filteredItems = new ArrayList<>();
            for (RenderItem item : allItems) {
                if (item.text().toLowerCase().contains(filterText.toLowerCase())) {
                    filteredItems.add(item);
                }
            }
        }

        if (filteredItems.isEmpty()) {
            selectedIndex = 0;
        } else if (selectedIndex >= filteredItems.size()) {
            selectedIndex = filteredItems.size() - 1;
        }

        redraw();
    }

    @Override
    protected void redraw() {
        if (filteredItems.isEmpty()) {
            ctx.console().errorf("\r%s%s%n", ctx.colors().NEUTRAL_400.use("> Oops! No matches found for "), formatFilter(filterText));
            ctx.console().errorf("%n\r@!Esc!@ to cancel, @!Backspace!@ to remove filter%n");
        } else {
            for (int i = 0; i < filteredItems.size(); i++) {
                drawItem(i);
            }
            ctx.console().errorf("%n\r@!↑↓!@ navigate, @!Space!@ toggle, @!Ctrl+A!@ select all, @!Enter!@ confirm, @!type!@ to filter%n");
        }
    }

    @Override
    protected Optional<List<T>> handleSelection() {
        clearSelector();

        if (!req.clearAfterSelection()) {
            ctx.console().errorf("@!> %d items selected!@\r\n\r\n", checkedOptions.size());
        }

        val result = new ArrayList<T>();
        for (String opt : req.options()) {
            if (checkedOptions.contains(opt)) {
                result.add(req.mapper().apply(opt));
            }
        }

        return Optional.of(result);
    }

    private void handleSpace() {
        if (!filteredItems.isEmpty()) {
            val item = filteredItems.get(selectedIndex);
            if (checkedOptions.contains(item.optionValue())) {
                checkedOptions.remove(item.optionValue());
            } else {
                checkedOptions.add(item.optionValue());
            }
            clearSelector();
            redraw();
        }
    }

    private void handleSelectAll() {
        if (!filteredItems.isEmpty()) {
            val allChecked = filteredItems.stream()
                .allMatch(item -> checkedOptions.contains(item.optionValue()));
            if (allChecked) {
                filteredItems.forEach(item -> checkedOptions.remove(item.optionValue()));
            } else {
                filteredItems.forEach(item -> checkedOptions.add(item.optionValue()));
            }
            clearSelector();
            redraw();
        }
    }

    private void buildAllItems() {
        for (String opt : req.options()) {
            allItems.add(new RenderItem(opt, opt));
        }
    }

    private void drawItem(int index) {
        val item = filteredItems.get(index);

        val isFocused = index == selectedIndex;
        val isChecked = checkedOptions.contains(item.optionValue());

        val cursor   = isFocused ? ctx.highlight(">") : " ";
        val checkbox = isChecked ? ctx.highlight("[x]") : "[ ]";

        val optionStr = isFocused
            ? ctx.highlight(highlightMatch(item.text()), false)
            : highlightMatch(item.text());

        ctx.console().errorf("\r%s %s %s%n", cursor, checkbox, optionStr);
    }
}
