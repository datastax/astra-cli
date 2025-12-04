package com.dtsx.astra.cli.core.models;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.Highlightable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CdcRef implements Highlightable {
    private final DbRef dbRef;
    private final Either<CdcId, Pair<TableRef, TenantName>> ref;

    public static CdcRef fromId(DbRef dbRef, CdcId id) {
        return new CdcRef(dbRef, Either.left(id));
    }

    public static CdcRef fromDefinition(TableRef tableRef, TenantName tenantName) {
        return new CdcRef(tableRef.db(), Either.pure(Pair.of(tableRef, tenantName)));
    }

    public boolean isId() {
        return ref.isLeft();
    }

    public boolean isDefinition() {
        return ref.isRight();
    }

    public DbRef db() {
        return dbRef;
    }

    public <T> T fold(Function<CdcId, T> idMapper, BiFunction<TableRef, TenantName, T> defMapper) {
        return ref.fold(
            idMapper,
            pair -> defMapper.apply(pair.getLeft(), pair.getRight())
        );
    }

    @JsonValue
    public Map<String, Object> toJson() {
        return ref.fold(
            id -> sequencedMapOf("type", "id", "value", id.toString()),
            ref -> sequencedMapOf("type", "ref", "value", sequencedMapOf("table", ref.getLeft(), "tenant", ref.getRight()))
        );
    }

    @Override
    public String toString() {
        return fold(CdcId::toString, "(table=%s,tenant=%s)"::formatted);
    }

    @Override
    public String highlight(CliContext ctx) {
        return ctx.highlight(toString());
    }
}
