package com.example.swelabs;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Lab 2: the JavaFX application. Reads the valid users from users.txt
 * and opens the login window.
 */
public class LoginApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        ArrayList<User> users;
        try (Scanner in = new Scanner(new File("users.txt"))) {
            users = UsersApp.readUsers(in);
        }

        FXMLLoader loader = new FXMLLoader(LoginApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(loader.load(), 320, 240);
        LoginController controller = loader.getController();
        controller.setUsers(users);

        stage.setTitle("Users Login");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit()); // X ends the program
        stage.show();
    }
}
