package com.example.swelabs;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Controller of the login window. It stays thin: it reads the fields, starts a
 * LoginTask on a new thread and shows the result. It does no counting and no
 * timing: that is done by AttemptTracker, the FailureWorkers and CountdownTask.
 */
public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button loginButton;
    @FXML
    private Button syncTestButton;
    @FXML
    private Label syncTestLabel;

    private List<User> users;
    private AttemptTracker tracker;
    private BlockingQueue<FailedAttempt> failures;
    private Thread countdownThread;

    // Called once by LoginApplication, before the window is shown.
    public void setUp(List<User> users, AttemptTracker tracker, BlockingQueue<FailedAttempt> failures) {
        this.users = users;
        this.tracker = tracker;
        this.failures = failures;
    }

    // Starts a LoginTask on a new thread. The fields and the button are disabled
    // until the result comes back, so a double click cannot start two logins.
    @FXML
    private void onLoginButtonClick() {
        setFormDisabled(true);
        errorLabel.setText("");

        LoginTask task = new LoginTask(usernameField.getText().trim(), passwordField.getText(),
                users, tracker, failures, this);
        Thread thread = new Thread(task, "login-task");
        thread.setDaemon(true);
        thread.start();
    }

    // Called by LoginTask through Platform.runLater, so it runs on the JavaFX thread.
    void showResult(LoginResult result) {
        switch (result.getType()) {
            case SUCCESS:
                showWelcomeScreen();
                break;
            case ERROR:
                if (result.getAttemptsLeft() == LoginResult.NOT_TRACKED) {
                    errorLabel.setText("פרטים שגויים");
                } else {
                    errorLabel.setText("פרטים שגויים. ניסיונות שנותרו: " + result.getAttemptsLeft());
                }
                setFormDisabled(false);
                break;
            case BLOCKED:
                // The form stays disabled. A CountdownTask on its own thread updates the
                // message every second and opens the form again when the block ends.
                countdownThread = new Thread(new CountdownTask(result.getSecondsLeft(), this), "countdown");
                countdownThread.setDaemon(true);
                countdownThread.start();
                break;
        }
    }

    // Called by CountdownTask once a second (on the JavaFX thread).
    void showCountdown(int secondsLeft) {
        errorLabel.setText("החשבון חסום, נותרו " + secondsLeft + " שניות");
    }

    // Called by CountdownTask when the block is over (on the JavaFX thread).
    void countdownFinished() {
        errorLabel.setText("");
        setFormDisabled(false);
    }

    // Called when the window closes: stops a countdown that is still running.
    public void stopCountdown() {
        if (countdownThread != null) {
            countdownThread.interrupt();
        }
    }

    // Runs the 20-thread test of SyncTest on its own thread, so the window stays
    // responsive, and shows the report when the test is done.
    @FXML
    private void onSyncTestButtonClick() {
        syncTestButton.setDisable(true);
        syncTestLabel.setText("Running the sync test...");

        Thread thread = new Thread(() -> {
            String report = SyncTest.run();
            System.out.println(report);
            Platform.runLater(() -> {
                syncTestLabel.setText(report);
                syncTestButton.setDisable(false);
            });
        }, "sync-test");
        thread.setDaemon(true);
        thread.start();
    }

    private void setFormDisabled(boolean disabled) {
        usernameField.setDisable(disabled);
        passwordField.setDisable(disabled);
        loginButton.setDisable(disabled);
    }

    // Replaces the login scene with the welcome scene in the same window.
    private void showWelcomeScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("welcome-view.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 320, 240));
        } catch (IOException e) {
            errorLabel.setText("Could not open the welcome screen");
            setFormDisabled(false);
        }
    }
}
