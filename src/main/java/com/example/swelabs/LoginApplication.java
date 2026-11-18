package com.example.swelabs;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The JavaFX application.
 * Lab 2: reads the valid users from users.txt and opens the login window.
 * Lab 3: takes n and t from the program arguments, checks them first, and
 * starts the two FailureWorker threads that count the failed logins.
 */
public class LoginApplication extends Application {
    private int maxAttempts;    // n: failure number n blocks the account
    private int blockSeconds;   // t: how long a block lasts, in seconds
    private ArrayList<User> users;

    private AttemptTracker tracker;
    private BlockingQueue<FailedAttempt> failures;
    private FailureWorker[] workers;
    private LoginController controller;

    // JavaFX calls init() before start(), and not on the JavaFX thread.
    // So the arguments are checked and the users file is read before any
    // window opens, and the JavaFX thread never reads a file or waits for input.
    @Override
    public void init() throws IOException {
        readArguments();
        try (Scanner in = new Scanner(new File("users.txt"))) {
            users = UsersApp.readUsers(in);
        }

        // One tracker and one queue are shared by all the threads.
        tracker = new AttemptTracker(maxAttempts, blockSeconds);
        failures = new LinkedBlockingQueue<>();
        workers = new FailureWorker[] {
                new FailureWorker("failure-worker-1", failures, tracker),
                new FailureWorker("failure-worker-2", failures, tracker)
        };
        for (FailureWorker worker : workers) {
            worker.start();
        }
    }

    // n and t are the two program arguments, for example: 3 30
    // If they are missing or wrong, the user is asked to type them in the console.
    private void readArguments() {
        List<String> args = getParameters().getRaw();
        maxAttempts = args.size() >= 1 ? parsePositiveInt(args.get(0)) : -1;
        blockSeconds = args.size() >= 2 ? parsePositiveInt(args.get(1)) : -1;

        if (maxAttempts == -1 || blockSeconds == -1) {
            System.out.println("Missing or wrong arguments: " + args);
            System.out.println("n (max wrong attempts) and t (block seconds) must be whole numbers of 1 or more.");
            Scanner console = new Scanner(System.in);
            maxAttempts = askForPositiveInt(console, "Enter n: ");
            blockSeconds = askForPositiveInt(console, "Enter t: ");
        }
        System.out.println("n = " + maxAttempts + ", t = " + blockSeconds + " seconds");
    }

    // Returns the number, or -1 if the text is not a whole number of 1 or more.
    private static int parsePositiveInt(String text) {
        try {
            int value = Integer.parseInt(text.trim());
            return value >= 1 ? value : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Asks again and again until the user types a whole number of 1 or more.
    private static int askForPositiveInt(Scanner console, String prompt) {
        while (true) {
            System.out.print(prompt);
            int value = parsePositiveInt(console.nextLine());
            if (value != -1) {
                return value;
            }
            System.out.println("Please type a whole number of 1 or more.");
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(LoginApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(loader.load(), 320, 240);
        controller = loader.getController();
        controller.setUp(users, tracker, failures);

        stage.setTitle("Users Login");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit()); // X ends the program
        stage.show();
    }

    // JavaFX calls stop() when the program is closing. The threads that may still
    // be sleeping or waiting are interrupted, so they end at once.
    @Override
    public void stop() {
        if (controller != null) {
            controller.stopCountdown();
        }
        for (FailureWorker worker : workers) {
            worker.interrupt();
        }
    }
}
