package com.dtsx.astra.cli.core.output.select;

public sealed interface SelectStatus<T> {
    record Selected<T>(T value) implements SelectStatus<T> {}
    
    record Default<T>(T value) implements SelectStatus<T> {}
    
    record NoAnswer<T>() implements SelectStatus<T> {}
}
