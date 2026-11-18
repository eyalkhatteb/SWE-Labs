package com.example.swelabs;

import java.util.concurrent.BlockingQueue;

/**
 * A worker thread that takes failed logins from the queue, counts them in the
 * tracker and sends back the answer. The program runs two of these.
 * It is a daemon thread, so it never keeps the program alive after the window closes.
 */
public class FailureWorker extends Thread {
    private final BlockingQueue<FailedAttempt> failures;
    private final AttemptTracker tracker;

    public FailureWorker(String name, BlockingQueue<FailedAttempt> failures, AttemptTracker tracker) {
        super(name);
        this.failures = failures;
        this.tracker = tracker;
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while (true) {
                // take() puts this thread to sleep until a failure arrives: no busy wait.
                FailedAttempt attempt = failures.take();
                attempt.answer(tracker.recordFailure(attempt.getEmail()));
            }
        } catch (InterruptedException e) {
            // interrupt() was called because the program is closing: the thread ends.
        }
    }
}
