package com.dtsx.astra.cli.core;

import com.dtsx.astra.cli.core.config.AstraHome;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.AstraLogger.Level;
import com.dtsx.astra.cli.core.output.Highlightable;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.GatewayProvider;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.Value;
import lombok.With;
import lombok.experimental.Accessors;
import lombok.val;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.ColorScheme;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@With
@Value
@Accessors(fluent = true)
public class CliContext {
    boolean isWindows;
    boolean isTty;
    OutputType outputType;
    AstraColors colors;
    AstraLogger log;
    AstraConsole console;
    AstraHome home;
    FileSystem fs;
    GatewayProvider gateways;

    // necessary for testing purpose only
    Optional<Profile> forceProfile;

    public Path path(String first, String... more) {
        return fs.getPath(first, more);
    }

    public boolean ansiEnabled() {
        return colors.ansi().enabled();
    }

    public Level logLevel() {
        return log.level();
    }

    public boolean isNotTty() {
        return !isTty;
    }

    public boolean isNotWindows() {
        return !isWindows;
    }

    public boolean outputIsHuman() {
        return outputType.isHuman();
    }

    public boolean outputIsNotHuman() {
        return outputType.isNotHuman();
    }

    public ColorScheme colorScheme() {
        return new Help.ColorScheme.Builder(AstraColors.DEFAULT_COLOR_SCHEME)
            .ansi(colors.ansi())
            .build();
    }

    public String highlight(String s) {
        return colors.BLUE_300.useOrQuote(s);
    }

    public String highlight(Path p) {
        return highlight(p.toString());
    }

    public String highlight(UUID u) {
        return highlight(u.toString());
    }

    public String highlight(long l) {
        return ansiEnabled() ? colors.BLUE_300.use(String.valueOf(l)) : String.valueOf(l);
    }

    public String highlight(Highlightable h) {
        return h.highlight(this);
    }

    public String highlight(DatabaseStatusType status) {
        if (!ansiEnabled()) {
            return "'" + status.name() + "'";
        }

        val color = switch (status) {
            case ACTIVE -> colors.GREEN_500;
            case ERROR, TERMINATED, UNKNOWN -> colors.RED_500;
            case DECOMMISSIONING, TERMINATING, DEGRADED -> colors.YELLOW_500;
            case HIBERNATED, PARKED, PREPARED -> colors.BLUE_500;
            case INITIALIZING, PENDING, HIBERNATING, PARKING, MAINTENANCE, PREPARING, RESIZING, RESUMING, UNPARKING -> colors.YELLOW_300;
            default -> colors.NEUTRAL_500;
        };

        return color.use(status.name());
    }
}
