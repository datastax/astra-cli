package com.dtsx.astra.cli.core;

import com.dtsx.astra.cli.core.config.AstraHome;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.AstraLogger.Level;
import com.dtsx.astra.cli.core.output.Highlightable;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.core.properties.CliEnvironment;
import com.dtsx.astra.cli.core.properties.CliEnvironment.OS;
import com.dtsx.astra.cli.core.properties.CliProperties;
import com.dtsx.astra.cli.gateways.GatewayProvider;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroupStatusType;
import com.dtsx.astra.cli.utils.FileUtils;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.Value;
import lombok.With;
import lombok.val;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@With
@Value
public class CliContext {
    CliEnvironment env;
    CliProperties properties;
    OutputType outputType;
    AstraColors colors;
    AstraLogger log;
    AstraConsole console;
    AstraHome home;
    FileSystem fs;
    GatewayProvider gateways;
    Consumer<CliContext> upgradeNotifier;

    // necessary for testing purpose only
    Optional<Profile> forceProfileForTesting;

    public Path path(String first, String... more) {
        return FileUtils.expandTilde(this, fs.getPath(first, more));
    }

    public Path absPath(String first, String... more) {
        return FileUtils.toAbsPath(this, path(first, more));
    }

    public boolean ansiEnabled() {
        return colors.ansi().enabled();
    }

    public Level logLevel() {
        return log.level();
    }

    public boolean isTty() {
        return env.isTty();
    }

    public boolean isNotTty() {
        return !isTty();
    }

    public boolean isWindows() {
        return env().platform().os() == OS.WINDOWS;
    }

    public boolean isNotWindows() {
        return !isWindows();
    }

    public boolean outputIsHuman() {
        return outputType.isHuman();
    }

    public boolean outputIsNotHuman() {
        return outputType.isNotHuman();
    }

    public String highlight(String s) {
        return highlight(s, true);
    }

    public String highlight(String s, boolean orQuote) {
        return colors.highlight(s, orQuote);
    }

    public String highlight(Path p) {
        return highlight(p.toString());
    }

    public String highlight(UUID u) {
        return highlight(u.toString());
    }

    public String highlight(long l) {
        return highlight(Long.toString(l), false);
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
            case INITIALIZING, PENDING, HIBERNATING, PARKING, MAINTENANCE, PREPARING, RESIZING, RESUMING, UNPARKING, ASSOCIATING -> colors.YELLOW_300;
            default -> colors.NEUTRAL_500;
        };

        return color.use(status.name());
    }

    public String highlight(PcuGroupStatusType status) {
        if (!ansiEnabled()) {
            return "'" + status.name() + "'";
        }

        val color = switch (status) {
            case CREATED, ACTIVE -> colors.GREEN_500;
            case PARKED -> colors.BLUE_500;
            case INITIALIZING, PLACING, PARKING, UNPARKING -> colors.YELLOW_300;
        };

        return color.use(status.name());
    }
}
