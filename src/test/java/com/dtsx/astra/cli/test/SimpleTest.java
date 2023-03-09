package com.dtsx.astra.cli.test;

import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import org.junit.jupiter.api.Test;

public class SimpleTest {

    @Test
    public void shouldThrowInvalid() {
        CloudProviderType.valueOf("");
    }
}
