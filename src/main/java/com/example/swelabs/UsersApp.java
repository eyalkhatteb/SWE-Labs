package com.example.swelabs;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

/**
 * Lab 1: reads users from standard input, reports the invalid lines, and prints
 * the valid users sorted by username, to the screen and to output.txt.
 */
public class UsersApp {

    // Reads "username password" lines from any Scanner:
    // System.in for the Lab 1 test, a file for Lab 2.
    // Invalid lines are reported on System.err and skipped.
    public static ArrayList<User> readUsers(Scanner in) {
        ArrayList<User> users = new ArrayList<>();
        while (in.hasNextLine()) {
            String line = in.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\s+");
            if (parts.length != 2) {
                System.err.println(line + "  -->  expected a username and a password");
                continue;
            }
            try {
                users.add(new User(parts[0], parts[1]));
            } catch (InvalidUserException e) {
                System.err.println(line + "  -->  " + e.getMessage());
            }
        }
        return users;
    }

    public static void main(String[] args) {
        ArrayList<User> users = readUsers(new Scanner(System.in));
        users.sort(Comparator.comparing(User::getUsername));

        try (PrintWriter out = new PrintWriter("output.txt")) {
            for (User u : users) {
                System.out.print(u.toOutputLine() + "\n");
                out.print(u.toOutputLine() + "\n");
            }
        } catch (FileNotFoundException e) {
            System.err.println("Could not write output.txt: " + e.getMessage());
        }
    }
}
