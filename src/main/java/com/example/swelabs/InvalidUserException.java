package com.example.swelabs;

/**
 * Thrown when a username or a password breaks one of the rules.
 * The message says which rule.
 */
public class InvalidUserException extends Exception {
    public InvalidUserException(String message) {
        super(message);
    }
}
