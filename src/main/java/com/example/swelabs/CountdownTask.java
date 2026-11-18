package com.example.swelabs;

import javafx.application.Platform;

/**
 * Shows a live countdown while an account is blocked. It runs on its own thread,
 * because sleeping on the JavaFX thread would freeze the window. Once a second it
 * asks the JavaFX thread to update the message, and at the end to open the form again.
 */
public class CountdownTask implements Runnable {
    private final int seconds;
    private final LoginController controller;

    public CountdownTask(int seconds, LoginController controller) {
        this.seconds = seconds;
        this.controller = controller;
    }

    @Override
    public void run() {
        try {
            for (int left = seconds; left > 0; left--) {
                int secondsLeft = left;    // a lambda may only use a variable that never changes
                Platform.runLater(() -> controller.showCountdown(secondsLeft));
                Thread.sleep(1000);
            }
            Platform.runLater(controller::countdownFinished);
        } catch (InterruptedException e) {
            // The window was closed in the middle of the countdown: stop counting.
        }
    }
}
