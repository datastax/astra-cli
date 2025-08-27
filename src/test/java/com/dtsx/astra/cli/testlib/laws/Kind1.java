package com.dtsx.astra.cli.testlib.laws;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;

public interface Kind1<A> {
    @Provide
    Arbitrary<A> anyA();
}
