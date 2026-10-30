package com.dtsx.astra.cli.unit.core.output;

import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.output.prompters.PromptRequest;
import com.dtsx.astra.cli.core.output.prompters.strategies.ArrowKeySelectionStrategy;
import com.dtsx.astra.cli.core.output.prompters.strategies.CheckboxSelectionStrategy;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext;
import com.dtsx.astra.cli.testlib.extensions.context.UseTestCtx;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class SelectionStrategyWindowingTest {

    @UseTestCtx
    private TestCliContext ctx;

    @Test
    void testArrowKeySelectionStrategyWindowing() {
        val options = new ArrayList<String>();
        for (int i = 1; i <= 30; i++) {
            options.add("Role " + i);
        }
        val neList = NEList.parse(options).orElseThrow();
        val req = new PromptRequest.Closed<String>(
            "Select role:",
            Optional.of("Role 1"),
            Function.identity(),
            true,
            neList,
            true
        );

        val strategy = new ArrowKeySelectionStrategy<String>(ctx.get(), req);
        assertThat(strategy).isNotNull();
    }

    @Test
    void testCheckboxSelectionStrategyWindowing() {
        val options = new ArrayList<String>();
        for (int i = 1; i <= 30; i++) {
            options.add("Role " + i);
        }
        val neList = NEList.parse(options).orElseThrow();
        val req = new PromptRequest.MultiClosed<String>(
            "Select roles:",
            List.of("Role 1"),
            Function.identity(),
            true,
            neList
        );

        val strategy = new CheckboxSelectionStrategy<String>(ctx.get(), req);
        assertThat(strategy).isNotNull();
    }
}
