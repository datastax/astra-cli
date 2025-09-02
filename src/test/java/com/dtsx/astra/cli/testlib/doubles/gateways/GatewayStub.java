package com.dtsx.astra.cli.testlib.doubles.gateways;

import com.spun.util.tests.TestUtils;
import lombok.val;
import org.approvaltests.namer.AttributeStackSelector;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class GatewayStub {
    private final Map<String, Integer> targetCallCounts = new HashMap<>();
    private final Map<String, Integer> actualCallCounts = new ConcurrentHashMap<>();

    public final void requireCalled(int times) {
        val info = TestUtils.getCurrentFileForMethod(new AttributeStackSelector());
    }
}
