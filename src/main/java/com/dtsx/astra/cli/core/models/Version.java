package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.output.Highlightable;
import lombok.*;
import lombok.experimental.Accessors;
import org.graalvm.collections.Pair;

import java.util.Optional;
import java.util.regex.Pattern;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Version implements Highlightable, Comparable<Version> {
    private static final Pattern VERSION_PATTERN = Pattern.compile("^v?(\\d+)\\.(\\d+)(?:\\.(\\d+))?(?:-([a-zA-Z0-9-_]+\\.\\d+))?$");

    private final int major;
    private final int minor;
    private final int patch;
    private final Optional<Pair<String, Integer>> label;

    public static Either<String, Version> parse(String version) {
        return ModelUtils.trimAndValidateBasics("Version", version).flatMap((trimmed) -> {
            val matcher = VERSION_PATTERN.matcher(trimmed);

            if (matcher.matches()) {
                val major = Integer.parseInt(matcher.group(1));
                val minor = Integer.parseInt(matcher.group(2));
                val patch = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;

                val preRelease = Optional.ofNullable(matcher.group(4))
                    .map((pr) -> {
                        val parts = pr.split("\\.");
                        return Pair.create(parts[0].toLowerCase(), Integer.parseInt(parts[1]));
                    });

                return Either.pure(new Version(major, minor, patch, preRelease));
            } else {
                return Either.left("Invalid version format: " + trimmed + " (expected format: x.y[.z][-<label>.n])");
            }
        });
    }

    public static Version mkUnsafe(String version) {
        return parse(version).getRight((_) -> new CongratsYouFoundABugException("Invalid version: " + version));
    }

    public boolean isPreRelease() {
        return label.isPresent();
    }

    enum KnownPreRelease { ALPHA, BETA, RC }

    @Override
    public int compareTo(Version other) {
        if (this.major != other.major) {
            return Integer.compare(this.major, other.major);
        }

        if (this.minor != other.minor) {
            return Integer.compare(this.minor, other.minor);
        }

        if (this.patch != other.patch) {
            return Integer.compare(this.patch, other.patch);
        }

        if (this.label.isEmpty() && other.label.isPresent()) {
            return 1; // release versions are greater than pre-release versions
        }

        if (this.label.isPresent() && other.label.isEmpty()) {
            return -1; // pre-release versions are less than release versions
        }

        if (this.label.isPresent()) {
            val thisPr = this.label.get();
            val otherPr = other.label.get();

            if (!thisPr.getLeft().equals(otherPr.getLeft())) {
                try {
                    val thisKnown = KnownPreRelease.valueOf(thisPr.getLeft().toUpperCase());
                    val otherKnown = KnownPreRelease.valueOf(otherPr.getLeft().toUpperCase());
                    return Integer.compare(thisKnown.ordinal(), otherKnown.ordinal());
                } catch (IllegalArgumentException e) {
                    return 1;
                }
            }

            return Integer.compare(thisPr.getRight(), otherPr.getRight());
        }

        return 0;
    }

    @Override
    public String highlight(CliContext ctx) {
        return ctx.highlight(toString());
    }

    @Override
    public String toString() {
        return "%d.%d.%d%s".formatted(major, minor, patch, label.map(pr -> "-%s.%d".formatted(pr.getLeft(), pr.getRight())).orElse(""));
    }
}
