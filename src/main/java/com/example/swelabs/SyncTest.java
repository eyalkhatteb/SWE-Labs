package com.example.swelabs;

import java.util.concurrent.CountDownLatch;

/**
 * A test that proves the failure counting is thread-safe. 20 threads record
 * failures for the same email at the same time, and not one update may be lost.
 * With n = 10,000: 20 threads x 499 failures = 9,980. After that, 19 more
 * failures (9,999) must not block, and failure number 10,000 must block.
 */
public class SyncTest {
    private static final int THREADS = 20;
    private static final int FAILURES_PER_THREAD = 499;
    private static final int MAX_ATTEMPTS = 10_000;
    private static final String EMAIL = "sync.test@mail.com";

    // Runs the test and returns a short report.
    public static String run() {
        AttemptTracker tracker = new AttemptTracker(MAX_ATTEMPTS, 30);
        CountDownLatch startSignal = new CountDownLatch(1);

        Thread[] threads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            threads[i] = new Thread(() -> {
                try {
                    startSignal.await();    // wait here, so all 20 threads start counting together
                    for (int j = 0; j < FAILURES_PER_THREAD; j++) {
                        tracker.recordFailure(EMAIL);
                    }
                } catch (InterruptedException e) {
                    // interrupted before the start: this thread records nothing
                }
            }, "sync-test-" + (i + 1));
            threads[i].start();
        }

        startSignal.countDown();
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            return "The sync test was interrupted";
        }

        int expected = THREADS * FAILURES_PER_THREAD;
        int counted = tracker.getFailures(EMAIL);

        // 19 more failures bring the counter to 9,999: still no block.
        boolean blockedTooEarly = false;
        for (int i = 0; i < 19; i++) {
            if (tracker.recordFailure(EMAIL).getType() == LoginResult.Type.BLOCKED) {
                blockedTooEarly = true;
            }
        }
        // Failure number 10,000 must block.
        boolean lastOneBlocked = tracker.recordFailure(EMAIL).getType() == LoginResult.Type.BLOCKED;

        boolean passed = counted == expected && !blockedTooEarly && lastOneBlocked;
        return "Sync test " + (passed ? "PASSED" : "FAILED") + "\n"
                + THREADS + " threads x " + FAILURES_PER_THREAD + " failures: counted " + counted + " of " + expected + "\n"
                + "19 more failures: " + (blockedTooEarly ? "blocked too early" : "not blocked") + "\n"
                + "Failure number " + MAX_ATTEMPTS + ": " + (lastOneBlocked ? "blocked" : "NOT blocked");
    }

    // Lets the test run from the command line too, without the window.
    public static void main(String[] args) {
        System.out.println(run());
    }
}
