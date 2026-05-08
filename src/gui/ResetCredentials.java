/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

/**
 *
 * @author Rhynne Gracelle
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class ResetCredentials {

    public static void main(String[] args) {
        Path appDir = Paths.get(System.getProperty("user.home"), ".motorph");

        try {
            if (!Files.exists(appDir)) {
                System.out.println("No .motorph folder found.");
                return;
            }

            Files.walk(appDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            System.out.println("Deleted: " + path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete: " + path + " -> " + e.getMessage());
                        }
                    });

            System.out.println("Credentials reset complete.");
        } catch (IOException e) {
            System.err.println("Failed to reset credentials: " + e.getMessage());
            e.printStackTrace();
        }
    }
}