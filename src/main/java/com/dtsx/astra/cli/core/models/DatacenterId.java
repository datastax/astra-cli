package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.Highlightable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

import java.util.UUID;
import java.util.regex.Pattern;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DatacenterId implements Highlightable {
    private final UUID dbId;
    private final int index;

    private static final Pattern DCID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}-\\d+$");

    public static Either<String, DatacenterId> parse(@NonNull String id) {
        return ModelUtils.trimAndValidateBasics("datacenter id", id).flatMap((trimmed) -> {
            if (!DCID_PATTERN.matcher(trimmed).matches()) {
                return Either.left("Datacenter ID must be of the format <database-uuid>-<region-index> (e.g., 123e4567-e89b-12d3-a456-426614174000-1)");
            }

            val parts = trimmed.split("-");
            val uuidPart = String.join("-", parts[0], parts[1], parts[2], parts[3], parts[4]);
            val indexPart = parts[5];

            try {
                val uuid = UUID.fromString(uuidPart);
                val index = Integer.parseInt(indexPart);

                if (index < 1) {
                    return Either.left("Region index must be a non-negative natural number.");
                }

                return Either.pure(mkUnsafe(uuid, index));
            } catch (NumberFormatException e) {
                return Either.left("Region index must be a valid integer.");
            } catch (IllegalArgumentException e) {
                return Either.left("Database UUID part is not a valid UUID.");
            }
        });
    }

    public static DatacenterId mkUnsafe(@NonNull UUID dbId, int index) {
        return new DatacenterId(dbId, index);
    }

    @JsonValue
    public String unwrap() {
        return dbId.toString() + "-" + index;
    }

    public DbRef db() {
        return DbRef.fromId(dbId);
    }

    @Override
    public String highlight(CliContext ctx) {
        return ctx.highlight(unwrap());
    }

    @Override
    public String toString() {
        return unwrap();
    }
}
