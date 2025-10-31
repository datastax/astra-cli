package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.Highlightable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;

import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PcuAssocTarget implements Highlightable {
    private final Either<DatacenterId, DbRef> ref;

    public static Either<String, PcuAssocTarget> parse(@NonNull String ref) {
        return ModelUtils.trimAndValidateBasics("database name/id or datacenter id", ref).flatMap((trimmed) -> {
            return DatacenterId.parse(trimmed)
                .map(PcuAssocTarget::fromDatacenterId)
                .flatMapLeft((e1) -> DbRef.parse(trimmed)
                    .map(PcuAssocTarget::fromDbRef)
                    .mapLeft((e2) -> "Errors parsing database name/id or datacenter id: '" + e1 + "' and '" + e2 + "'"));
        });
    }

    public static PcuAssocTarget fromDatacenterId(@NonNull DatacenterId datacenterId) {
        return new PcuAssocTarget(Either.left(datacenterId));
    }

    public static PcuAssocTarget fromDbRef(@NonNull DbRef dbRef) {
        return new PcuAssocTarget(Either.pure(dbRef));
    }

    public <T> T fold(Function<DatacenterId, T> idMapper, Function<DbRef, T> refMapper) {
        return ref.fold(idMapper, refMapper);
    }

    @JsonValue
    public Map<String, Object> toJson() {
        return ref.fold(
            dc -> sequencedMapOf("type", "datacenter", "value", dc.unwrap()),
            db -> sequencedMapOf("type", "database", "value", db.toJson())
        );
    }

    @Override
    @JsonValue
    public String toString() {
        return ref.fold(DatacenterId::toString, DbRef::toString);
    }

    @Override
    public String highlight(CliContext ctx) {
        return ctx.highlight(toString());
    }
}
