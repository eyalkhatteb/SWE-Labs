package com.example.swelabs;

/**
 * A system user: an email address as the username, and a password.
 * A User object can only be created from valid values.
 */
public class User {
    private final String username;
    private final String password;

    // Length limits
    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_PASSWORD_LENGTH = 12;
    private static final int MIN_PASSWORD_LENGTH = 8;

    // Email format: name, '@', a domain that starts with a letter or digit,
    // '.', and an ending of at least two letters
    private static final String EMAIL_REGEX =
            "[A-Za-z0-9._+%-]+@[A-Za-z0-9][A-Za-z0-9.-]*\\.[A-Za-z]{2,}";

    // Symbols allowed in a password: the keyboard's number row, plus - and =
    private static final String PASSWORD_SYMBOLS = "!@#$%^&*()_+=-";

    public User(String username, String password) throws InvalidUserException {
        checkUsername(username);
        checkPassword(password);
        this.username = username;
        this.password = password;
    }

    private void checkUsername(String username) throws InvalidUserException {
        if (!username.matches(EMAIL_REGEX)) {
            throw new InvalidUserException("Please enter a valid Email as username");
        }
        if (username.length() > MAX_USERNAME_LENGTH) {
            throw new InvalidUserException("Username is too long, try something shorter");
        }
    }

    private void checkPassword(String password) throws InvalidUserException {
        // Check the length first, so a password of the wrong length gets the length message
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidUserException("Your password is too short, add more characters");
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new InvalidUserException("Your password is too long, try a shorter one");
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;
        for (char c : password.toCharArray()) {
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                hasLetter = true;
            } else if (c >= '0' && c <= '9') {
                hasDigit = true;
            } else if (PASSWORD_SYMBOLS.indexOf(c) >= 0) {
                hasSymbol = true;
            } else {
                throw new InvalidUserException("Please enter a valid password");
            }
        }
        if (!hasDigit || !hasLetter || !hasSymbol) {
            throw new InvalidUserException("Please enter a valid password");
        }
    }

    public String getUsername() {
        return username;
    }

    public String toOutputLine() {
        return username + " " + password;
    }

    @Override
    public String toString() {
        return "User{username='" + username + "'}";
    }

    // Returns true only if both the username and the password equal this user's.
    public boolean matches(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }
}