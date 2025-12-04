package com.dtsx.astra.cli.snapshot;

import com.spun.util.tests.StackTraceReflectionResult;
import lombok.SneakyThrows;
import lombok.val;
import org.approvaltests.namer.ApprovalNamer;
import org.approvaltests.namer.NamerFactory;
import org.approvaltests.namer.StackTraceNamer;
import org.approvaltests.writers.Writer;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FolderBasedApprovalNamer implements ApprovalNamer {
    private final StackTraceNamer internal;
    private final Class<?> testClazz;

    public FolderBasedApprovalNamer(Class<?> testClazz) {
        this.internal = new StackTraceNamer();
        this.testClazz = testClazz;
    }

    public FolderBasedApprovalNamer(Class<?> testClazz, StackTraceNamer internal) {
        this.internal = internal;
        this.testClazz = testClazz;
    }

    @Override
    public String getApprovalName() {
        return info().getMethodName() + getAdditionalInformation();
    }

    @Override
    @SneakyThrows
    public String getSourceFilePath() {
        val currentFilePath = info().getSourceFile().getAbsolutePath();

        val fullClassName = testClazz.getName();

        if (!fullClassName.contains("SnapshotTest")) {
            throw new IllegalStateException("Test class '" + fullClassName + "' must end with 'SnapshotTest'");
        }

        val packageName = fullClassName.substring(0, fullClassName.lastIndexOf("."));
        val className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1).replace("SnapshotTest", "");

        if (!fullClassName.startsWith("com.dtsx.astra.cli.snapshot.commands.")) {
            throw new IllegalStateException("Test class '" + fullClassName + "' must be in package 'com.dtsx.astra.cli.snapshot.commands'");
        }

        val rootDir = currentFilePath.substring(0, currentFilePath.indexOf(packageName.replace('.', File.separatorChar)));

        val packageAsPath = Arrays.stream(packageName.replaceAll("com.dtsx.astra.cli.snapshot.commands.?", "").split("\\."))
            .filter(s -> !s.isEmpty())
            .map((sub) -> "_" + sub)
            .collect(Collectors.joining(File.separator));

        val path = new String[] { rootDir, NamerFactory.getApprovalBaseDirectory(), packageAsPath, className };
        return new File(String.join(File.separator, path)).getCanonicalPath() + File.separator;
    }

    @Override
    public File getApprovedFile(String extensionWithDot) {
        return new File(getSourceFilePath() + getApprovalName() + Writer.approved + extensionWithDot);
    }

    @Override
    public File getReceivedFile(String extensionWithDot) {
        return new File(getSourceFilePath() + getApprovalName() + Writer.received + extensionWithDot);
    }

    @Override
    public ApprovalNamer addAdditionalInformation(String info) {
        return new FolderBasedApprovalNamer(testClazz, (StackTraceNamer) internal.addAdditionalInformation(info));
    }

    @Override
    public String getAdditionalInformation() {
        return internal.getAdditionalInformation();
    }

    private StackTraceReflectionResult info() {
        return internal.getInfo();
    }
}
