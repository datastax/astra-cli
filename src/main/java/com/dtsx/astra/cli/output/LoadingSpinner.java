package com.dtsx.astra.cli.output;

import lombok.val;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LoadingSpinner {
    private static final String[] SPINNER_FRAMES = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
    private static final int FRAME_DELAY_MS = 80;

    private final AtomicReference<String> message;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicInteger lastLineLength = new AtomicInteger(0);
    private Thread spinnerThread;

    public String getMessage() {
        return message.get();
    }
    
    public LoadingSpinner(String initialMessage) {
        this.message = new AtomicReference<>(initialMessage);
    }
    
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            spinnerThread = new Thread(this::runSpinner);
            spinnerThread.start();
        }
    }
    
    public void stop() {
        isRunning.set(false);
        if (spinnerThread != null) {
            try {
                spinnerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        clearLine();
    }
    
    public void updateMessage(String newMessage) {
        message.set(newMessage);
    }
    
    public void pause() {
        isPaused.set(true);
        clearLine();
    }
    
    public void resume() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        isPaused.set(false);
    }
    
    private void runSpinner() {
        int frameIndex = 0;
        
        while (isRunning.get()) {
            if (!isPaused.get() && AstraLogger.getLevel() != AstraLogger.Level.QUIET) {
                val currentLine = AstraColors.BLUE_300.use(SPINNER_FRAMES[frameIndex] + " ") + message.get();
                val clearLine = "\r" + " ".repeat(lastLineLength.get()) + "\r";
                AstraConsole.error(clearLine + currentLine);
                lastLineLength.set(AstraColors.stripAnsi(currentLine).length());
            }
            
            frameIndex = (frameIndex + 1) % SPINNER_FRAMES.length;
            
            try {
                Thread.sleep(FRAME_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private void clearLine() {
        if (AstraLogger.getLevel() != AstraLogger.Level.QUIET) {
            val clearSpaces = Math.max(50, lastLineLength.get());
            AstraConsole.error("\r" + " ".repeat(clearSpaces) + "\r");
        }
    }
}