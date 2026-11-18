package com.example.swelabs;

/**
 * The login state of one email: how many wrong passwords in a row,
 * and until when the email is blocked.
 * Several threads can use the same UserState at the same time,
 * so every method is synchronized: one thread at a time.
 */
public class UserState {
    private int failures = 0;
    private long blockedUntilMillis = 0;   // 0 means "not blocked"

    // Counts one failed login. Failure number maxAttempts starts a block of
    // blockSeconds. A failure during a block is refused and is not counted.
    public synchronized LoginResult recordFailure(int maxAttempts, int blockSeconds) {
        int secondsLeft = secondsLeft();
        if (secondsLeft > 0) {
            return LoginResult.blocked(secondsLeft);
        }

        failures++;
        if (failures >= maxAttempts) {
            blockedUntilMillis = System.currentTimeMillis() + blockSeconds * 1000L;
            return LoginResult.blocked(blockSeconds);
        }
        return LoginResult.error(maxAttempts - failures);
    }

    // Seconds left in the block, rounded up. 0 means the email is not blocked.
    // When a block has ended, the counter is cleared: a new set of attempts.
    public synchronized int secondsLeft() {
        if (blockedUntilMillis == 0) {
            return 0;
        }
        long millisLeft = blockedUntilMillis - System.currentTimeMillis();
        if (millisLeft <= 0) {
            reset();
            return 0;
        }
        return (int) ((millisLeft + 999) / 1000);
    }

    // Called after a successful login, and when a block ends.
    public synchronized void reset() {
        failures = 0;
        blockedUntilMillis = 0;
    }

    public synchronized int getFailures() {
        return failures;
    }
}
