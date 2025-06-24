package com.dtsx.astra.cli.operations;

@FunctionalInterface
public interface Operation<R> {
    R execute();
}
