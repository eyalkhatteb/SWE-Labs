package com.example.swelabs;

import java.util.concurrent.ConcurrentHashMap;

/***
 * Keeps the login state of every email that has had a wrong password.
 * The state lives in memory only, so it is gone when the program closes.
 * One tracker is shared by all the threads, so a block belongs to the email
 * and not to a window: a second window or a parallel login cannot get around it.
 ***/
public class AttemptTracker {
    private final int maxAttempts;    // n: failure number n starts a block
    private final int blockSeconds;   // t: how long a block lasts
    private final ConcurrentHashMap<String, UserState> states = new ConcurrentHashMap<>();

    public AttemptTracker(int maxAttempts, int blockSeconds) {
        this.maxAttempts = maxAttempts;
        this.blockSeconds = blockSeconds;
    }

    // Seconds left in the block of this email. 0 means it is not blocked.
    public int secondsBlocked(String email) {
        UserState state = states.get(email);
        return state == null ? 0 : state.secondsLeft();
    }

    // Records one failed login of an existing email. The answer is an error
    // with the number of attempts left, or a block with the seconds left.
    public LoginResult recordFailure(String email) {
        UserState state = states.computeIfAbsent(email, key -> new UserState());
        return state.recordFailure(maxAttempts, blockSeconds);
    }

    // A successful login clears the failures of this email.
    public void recordSuccess(String email) {
        UserState state = states.get(email);
        if (state != null) {
            state.reset();
        }
    }

    // How many failures are counted for this email right now.
    public int getFailures(String email) {
        UserState state = states.get(email);
        return state == null ? 0 : state.getFailures();
    }
}
