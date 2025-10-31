package com.dtsx.astra.cli.gateways.pcu;

import com.dtsx.astra.cli.core.models.PcuRef;

import java.util.Optional;
import java.util.UUID;

public interface PcuCache {
    void cachePcuGroupId(String title, UUID id);
    Optional<UUID> lookupPcuGroupId(PcuRef ref);

    default PcuRef convertPcuTitleToIdIfCached(PcuRef ref) {
        return ref.fold(
            _ -> ref,
            _ -> lookupPcuGroupId(ref).map(PcuRef::fromId).orElse(ref)
        );
    }
}
