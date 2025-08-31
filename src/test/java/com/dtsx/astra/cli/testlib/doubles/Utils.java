package com.dtsx.astra.cli.testlib.doubles;

public class Utils {
    public static <T> T methodIllegallyCalled() {
        throw new AssertionError("A method was illegally called");
    }
}
