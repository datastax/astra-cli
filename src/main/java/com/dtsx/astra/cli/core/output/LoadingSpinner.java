package com.dtsx.astra.cli.core.output;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.properties.CliEnvironment;
import lombok.val;
import org.apache.commons.lang3.function.FailableRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.AstraColors.stripAnsi;
import static com.dtsx.astra.cli.core.properties.CliEnvironment.OS.WINDOWS;

public class LoadingSpinner {
    private final Supplier<CliContext> ctx;

    private final String[] SPINNER_FRAMES;
    private static final int FRAME_DELAY_MS = 80;

    private volatile String message;
    private int lastLineLength = 0;

    private volatile @Nullable Thread spinnerThread;
    private volatile boolean paused = false;

    private final Object activityLock = new Object();

    public LoadingSpinner(CliEnvironment cliEnv, Supplier<CliContext> ctxSupplier) {
        this.ctx = ctxSupplier;
        this.SPINNER_FRAMES = PlatformChars.spinnerFrames(cliEnv.platform().os() == WINDOWS);
    }

    public Optional<LoadingSpinnerControls> start(String message) {
        if (spinnerThread != null) {
            return Optional.empty();
        }

        this.message = message;
        this.spinnerThread = Thread.ofVirtual().start(this::runSpinner);

        return Optional.of(new LoadingSpinnerControls());
    }

    public class LoadingSpinnerControls {
        public void updateMessage(String newMessage) {
            message = newMessage;
        }

        public void stop() {
            val thread = spinnerThread;
            spinnerThread = null;
            paused = false;

            if (thread != null) {
                synchronized (activityLock) {
                    activityLock.notifyAll();
                }

                try {
                    thread.interrupt();
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            clearLine();
        }
    }

    public Runnable pause() {
        paused = true;
        clearLine();

        return () -> {
            paused = false;
            synchronized (activityLock) {
                activityLock.notifyAll();
            }
        };
    }

    private void runSpinner() {
        var frameIndex = 0;

        while (Thread.currentThread() == spinnerThread) {
            synchronized (activityLock) {
                while (paused && Thread.currentThread() == spinnerThread) {
                    catchInterrupt(activityLock::wait);
                }
            }

            clearLine();

            val currentLine = ctx.get().colors().BLUE_300.use(SPINNER_FRAMES[frameIndex]) + " " + message + "...";

            System.err.print(currentLine);
            System.err.flush();

            lastLineLength = stripAnsi(currentLine).length();
            frameIndex = (frameIndex + 1) % SPINNER_FRAMES.length;

            catchInterrupt(() -> Thread.sleep(FRAME_DELAY_MS));
        }
    }

    private void clearLine() {
        if (lastLineLength > 0) {
            System.err.print("\r" + " ".repeat(lastLineLength) + "\r");
            System.err.flush();
            lastLineLength = 0;
        }
    }

    private void catchInterrupt(FailableRunnable<InterruptedException> runnable) {
        try {
            runnable.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
