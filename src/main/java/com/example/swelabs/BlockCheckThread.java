package com.example.swelabs;

/**
 * A short thread that checks whether an email is blocked right now.
 * LoginTask starts it, waits for it with join(), and then reads the answer.
 */
public class BlockCheckThread extends Thread {
    private final AttemptTracker tracker;
    private final String email;
    private int secondsLeft;    // the answer: 0 means "not blocked"

    public BlockCheckThread(AttemptTracker tracker, String email) {
        super("block-check");
        this.tracker = tracker;
        this.email = email;
    }

    @Override
    public void run() {
        secondsLeft = tracker.secondsBlocked(email);
    }

    // Read this only after join(): by then run() has finished.
    public int getSecondsLeft() {
        return secondsLeft;
    }
}
