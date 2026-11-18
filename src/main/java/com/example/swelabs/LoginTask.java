package com.example.swelabs;

import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * One login attempt, from the click to the result. Every click on Login runs a
 * new LoginTask on its own thread, so the JavaFX thread never waits.
 * When the result is ready it goes to the controller through Platform.runLater,
 * because only the JavaFX thread may change the window.
 */
public class LoginTask implements Runnable {
    private final String email;
    private final String password;
    private final List<User> users;
    private final AttemptTracker tracker;
    private final BlockingQueue<FailedAttempt> failures;
    private final LoginController controller;

    public LoginTask(String email, String password, List<User> users, AttemptTracker tracker,
                     BlockingQueue<FailedAttempt> failures, LoginController controller) {
        this.email = email;
        this.password = password;
        this.users = users;
        this.tracker = tracker;
        this.failures = failures;
        this.controller = controller;
    }

    @Override
    public void run() {
        try {
            LoginResult result = login();
            Platform.runLater(() -> controller.showResult(result));
        } catch (InterruptedException e) {
            // The program is closing while this task was waiting: nothing to show.
        }
    }

    private LoginResult login() throws InterruptedException {
        // 1. Is the email blocked? A separate thread checks, and this thread waits for it.
        BlockCheckThread blockCheck = new BlockCheckThread(tracker, email);
        blockCheck.start();
        blockCheck.join();
        if (blockCheck.getSecondsLeft() > 0) {
            // Refused at once: the password is not checked and nothing is counted.
            return LoginResult.blocked(blockCheck.getSecondsLeft());
        }

        // 2. Check the details with the Lab 1 code.
        User user = findUser();
        if (user == null) {
            return LoginResult.unknownEmail();    // no such email: an error without tracking
        }
        if (passwordIsCorrect(user)) {
            tracker.recordSuccess(email);         // a successful login clears the counter
            return LoginResult.success();
        }

        // 3. Wrong password for an existing email: put the failure in the queue
        //    and wait until a FailureWorker has counted it and answers.
        FailedAttempt attempt = new FailedAttempt(email);
        failures.put(attempt);
        return attempt.waitForAnswer();
    }

    // Returns the valid user with this email, or null if there is none.
    private User findUser() {
        for (User user : users) {
            if (user.getUsername().equals(email)) {
                return user;
            }
        }
        return null;
    }

    // The typed details first go through the User constructor of Lab 1. It throws
    // InvalidUserException when they break the rules, and then they cannot be right.
    private boolean passwordIsCorrect(User user) {
        try {
            new User(email, password);
            return user.matches(email, password);
        } catch (InvalidUserException e) {
            return false;
        }
    }
}
