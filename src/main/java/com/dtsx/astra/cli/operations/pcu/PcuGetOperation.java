package com.dtsx.astra.cli.operations.pcu;

import com.dtsx.astra.cli.commands.pcu.PcuGetCmd.PcuGetKeys;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroup;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.pcu.PcuGetOperation.PcuInfo;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;

@RequiredArgsConstructor
public class PcuGetOperation implements Operation<PcuInfo> {
    private final PcuGateway pcuGateway;
    private final PcuGetRequest request;

    public record PcuGetRequest(
        PcuRef pcuRef,
        Optional<PcuGetKeys> key
    ) {}

    public sealed interface PcuInfo {}
    public record PcuInfoFull(PcuGroup pcuGroup) implements PcuInfo {}
    public record PcuInfoValue(Object value) implements PcuInfo {}

    @Override
    public PcuInfo execute() {
        val pcuGroup = pcuGateway.findOne(request.pcuRef);

        return request.key
            .map(key -> mkPcuInfoValue(key, pcuGroup))
            .orElseGet(() -> mkPcuInfoFull(pcuGroup));
    }

    private PcuInfo mkPcuInfoFull(PcuGroup pcuGroup) {
        return new PcuInfoFull(pcuGroup);
    }

    private PcuInfo mkPcuInfoValue(PcuGetKeys key, PcuGroup pcuGroup) {
        val value = switch (key) {
            case title -> pcuGroup.getTitle();
            case description -> pcuGroup.getDescription();
            case id -> pcuGroup.getId();
            case status -> pcuGroup.getStatus();
            case cloud -> pcuGroup.getCloudProvider();
            case region -> pcuGroup.getRegion();
            case type -> pcuGroup.getReserved() > 0 ? "committed" : "flexible";
            case min -> pcuGroup.getMin();
            case max -> pcuGroup.getMax();
            case reserved -> pcuGroup.getReserved();
        };

        return new PcuInfoValue(value);
    }
}
