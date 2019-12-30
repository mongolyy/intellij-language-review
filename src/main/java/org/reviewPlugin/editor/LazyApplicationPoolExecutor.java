package org.reviewPlugin.editor;

import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public class LazyApplicationPoolExecutor implements Executor {

    public static final int DEFAULT_DELAY = 100;

    private Runnable next;

    private Future<?> future;

    private int delay = DEFAULT_DELAY; // delay between command executions

    public LazyApplicationPoolExecutor(int delay) {
        this.delay = delay;
    }

    public LazyApplicationPoolExecutor() {
        this(DEFAULT_DELAY);
    }

    /**
     * Lazily executes the Runnable. Command will be queued for execution, but can be swallowed by another command if it
     * will be submitted before this command will be scheduled for execution
     *
     * @param command command to be executed.
     */
    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    public synchronized void execute(@NotNull final Runnable command) {
        next = () -> {
            try {
                command.run();
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                System.out.println(System.currentTimeMillis() + ": was interrupted");
                e.printStackTrace();
                Thread.currentThread().interrupt();
            } finally {
                if (!Thread.currentThread().isInterrupted()) {
                    scheduleNext(); //needed to execute the very last command
                }
            }
        };
        if (future == null || future.isDone()) {
            scheduleNext();
        }
    }

    private synchronized Future<?> scheduleNext() {
        final Runnable toSchedule = next;
        next = null;
        if (toSchedule != null) {
            future = ApplicationManager.getApplication().executeOnPooledThread(toSchedule);
            return future;
        }
        return null;
    }
}
