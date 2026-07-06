package com.dtsx.astra.cli.commands.pcu;

import com.dtsx.astra.cli.core.output.PlatformChars;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.sdk.pcu.domain.PCUGroup;
import com.dtsx.astra.cli.operations.pcu.PcuListOperation;
import lombok.val;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

public abstract class PcuListImpl extends AbstractPcuCmd<Stream<PCUGroup>> {
    @Override
    protected final OutputJson executeJson(Supplier<Stream<PCUGroup>> result) {
        return OutputJson.serializeValue(result.get().toList());
    }

    @Override
    protected final OutputAll execute(Supplier<Stream<PCUGroup>> result) {
        val flexibleKey = ctx.outputIsHuman()
            ? "F"
            : "Flexible";

        val data = result.get()
            .map((pcu) -> sequencedMapOf(
                "Title", title(pcu),
                "Id", id(pcu),
                "Cloud", cloud(pcu),
                "Region", region(pcu),
                flexibleKey, flexible(pcu),
                "Status", status(pcu)
            ))
            .toList();

        return new ShellTable(data).withColumns("Title", "Id", "Cloud", "Region", flexibleKey, "Status");
    }

    private String title(PCUGroup pcu) {
        return Objects.requireNonNullElse(pcu.getTitle(), "n/a");
    }

    private String id(PCUGroup pcu) {
        return pcu.getId().toString();
    }

    private String cloud(PCUGroup pcu) {
        return pcu.getCloudProvider().toString();
    }

    private String region(PCUGroup pcu) {
        return pcu.getRegion();
    }

    private String flexible(PCUGroup pcu) {
        return pcu.getReserved() == 0 ? PlatformChars.presenceIndicator(ctx.isWindows()) : "";
    }

    private String status(PCUGroup pcu) {
        return ctx.highlight(pcu.getStatus());
    }

    @Override
    protected PcuListOperation mkOperation() {
        return new PcuListOperation(pcuGateway);
    }
}
