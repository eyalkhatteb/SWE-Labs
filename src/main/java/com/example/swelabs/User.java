package com.example.swelabs;

public class User {
    private final String username;
    private final String password;

    //helper parameters for max/min length
    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_PASSWORD_LENGTH = 12;
    private static final int MIN_PASSWORD_LENGTH = 8;
    //helper parameter definition for email check
    private static final String EMAIL_REGEX =
            "[A-Za-z0-9._+%-]+@[A-Za-z0-9][A-Za-z0-9.-]*\\.[A-Za-z]{2,}";
    //helper parameter for password symbols definition
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
        //check length first
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
}