package com.example.swelabs;

/**
 * The outcome of one login attempt: success, an error, or a blocked account.
 * LoginTask builds it on a worker thread and hands it to the controller,
 * which only has to show it.
 */
public class LoginResult {
    public enum Type { SUCCESS, ERROR, BLOCKED }

    // The attemptsLeft value of an error for an email that is not in the users file.
    public static final int NOT_TRACKED = -1;

    private final Type type;
    private final int attemptsLeft;   // used when the type is ERROR
    private final int secondsLeft;    // used when the type is BLOCKED

    private LoginResult(Type type, int attemptsLeft, int secondsLeft) {
        this.type = type;
        this.attemptsLeft = attemptsLeft;
        this.secondsLeft = secondsLeft;
    }

    public static LoginResult success() {
        return new LoginResult(Type.SUCCESS, 0, 0);
    }

    // Wrong password for an existing email: says how many attempts are left.
    public static LoginResult error(int attemptsLeft) {
        return new LoginResult(Type.ERROR, attemptsLeft, 0);
    }

    // The email is not in the users file: an error without tracking.
    public static LoginResult unknownEmail() {
        return new LoginResult(Type.ERROR, NOT_TRACKED, 0);
    }

    // The account is blocked for this many more seconds.
    public static LoginResult blocked(int secondsLeft) {
        return new LoginResult(Type.BLOCKED, 0, secondsLeft);
    }

    public Type getType() {
        return type;
    }

    public int getAttemptsLeft() {
        return attemptsLeft;
    }

    public int getSecondsLeft() {
        return secondsLeft;
    }
}
