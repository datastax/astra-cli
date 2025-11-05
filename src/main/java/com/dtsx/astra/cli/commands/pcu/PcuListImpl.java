package com.dtsx.astra.cli.commands.pcu;

import com.dtsx.astra.cli.core.output.PlatformChars;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroup;
import com.dtsx.astra.cli.operations.pcu.PcuListOperation;
import lombok.val;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

public abstract class PcuListImpl extends AbstractPcuCmd<Stream<PcuGroup>> {
    @Override
    protected final OutputJson executeJson(Supplier<Stream<PcuGroup>> result) {
        return OutputJson.serializeValue(result.get().toList());
    }

    @Override
    protected final OutputAll execute(Supplier<Stream<PcuGroup>> result) {
        val data = result.get()
            .map((pcu) -> sequencedMapOf(
                "Title", title(pcu),
                "Id", id(pcu),
                "Cloud", cloud(pcu),
                "Region", region(pcu),
                "F", flexible(pcu),
                "Status", status(pcu)
            ))
            .toList();

        return new ShellTable(data).withColumns("Title", "Id", "Cloud", "Region", "F", "Status");
    }

    private String title(PcuGroup pcu) {
        return Objects.requireNonNullElse(pcu.getTitle(), "n/a");
    }

    private String id(PcuGroup pcu) {
        return pcu.getId();
    }

    private String cloud(PcuGroup pcu) {
        return pcu.getCloudProvider().toString();
    }

    private String region(PcuGroup pcu) {
        return pcu.getRegion();
    }

    private String flexible(PcuGroup pcu) {
        return pcu.getReserved() == 0 ? PlatformChars.presenceIndicator(ctx.isWindows()) : "";
    }

    private String status(PcuGroup pcu) {
        return ctx.highlight(pcu.getStatus());
    }

    @Override
    protected PcuListOperation mkOperation() {
        return new PcuListOperation(pcuGateway);
    }
}
