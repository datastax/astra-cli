package com.dtsx.astra.cli.utils;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@UtilityClass
public class MiscUtils {
    public static <A> Set<A> setAdd(Set<? extends A> init, A a) {
        val newSet = new HashSet<A>(init);
        newSet.add(a);
        return newSet;
    }

    public static <A> Set<A> setDel(Set<? extends A> init, A a) {
        val newSet = new HashSet<A>(init);
        newSet.remove(a);
        return newSet;
    }

    public static <A> List<A> listAdd(A a, List<? extends A> init) {
        val newList = new ArrayList<A>();
        newList.add(a);
        newList.addAll(init);
        return newList;
    }

    public static <A> List<A> listAdd(List<? extends A> init, A a) {
        val newList = new ArrayList<A>(init);
        newList.add(a);
        return newList;
    }

    public static <A> List<A> listConcat(List<? extends A> list1, List<? extends A> list2) {
        val newList = new ArrayList<A>(list1);
        newList.addAll(list2);
        return newList;
    }

    public static <T, R> Function<T, R> toFn(Consumer<T> r) {
        return t -> {
            r.accept(t);
            return null;
        };
    }

    public static <T1, T2, R> BiFunction<T1, T2, R> toFn(BiConsumer<T1, T2> r) {
        return (t1, t2) -> {
            r.accept(t1, t2);
            return null;
        };
    }

    // Shamelessly copied from PicoCLI's CommandLine class
    public static PrintWriter mkPrintWriter(OutputStream stream, @MagicConstant(stringValues = { "stdout", "stderr" }) String name) {
        return new PrintWriter(new BufferedWriter(new OutputStreamWriter(stream, charsetForName(System.getProperty("sun." + name + ".encoding")))), true);
    }

    // Shamelessly copied from PicoCLI's CommandLine class
    private static Charset charsetForName(@Nullable String encoding) {
        if (encoding != null) {
            try {
                return Charset.forName(("cp65001".equalsIgnoreCase(encoding)) ? "UTF-8" : encoding);
            } catch (Exception e) {
                return Charset.defaultCharset();
            }
        }
        return Charset.defaultCharset();
    }

    public static String captureStackTrace(Throwable t) {
        val sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
