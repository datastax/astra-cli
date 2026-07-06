package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.output.Highlightable;
import com.dtsx.astra.sdk.pcu.domain.PCUGroup;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.val;

@EqualsAndHashCode
public class PcuStatus implements Highlightable {
    private final String status;

    public static final PcuStatus ACTIVE = new PcuStatus("ACTIVE");
    public static final PcuStatus PARKED = new PcuStatus("PARKED");
    public static final PcuStatus PARKING = new PcuStatus("PARKING");
    public static final PcuStatus UNPARKING = new PcuStatus("UNPARKING");

    public PcuStatus(@NonNull String status) {
        this.status = status.toUpperCase();
    }

    public PcuStatus(@NonNull PCUGroup pcu) {
        this(pcu.getStatus());
    }

    @Override
    public String toString() {
        return status;
    }

    @Override
    public String highlight(CliContext ctx) {
        if (!ctx.ansiEnabled()) {
            return "'" + status + "'";
        }

        val color = switch (status) {
            case "CREATED", "ACTIVE" -> ctx.colors().GREEN_500;
            case "INITIALIZING", "PLACING", "PARKING", "UNPARKING" -> ctx.colors().YELLOW_300;
            default -> ctx.colors().BLUE_500;
        };

        return color.use(status);
    }
}
