package com.quickchat;

import java.util.Scanner;

public class QuickChat {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int totalSent = 0;

        System.out.println("=== QUICKCHAT REGISTRATION ===");
        System.out.print("Enter first name: ");
        String firstName = sc.nextLine();
        System.out.print("Enter last name: ");
        String lastName = sc.nextLine();

        Login login = new Login(firstName, lastName);

        System.out.print("Create a username (must contain _ and be max 5 chars): ");
        String username = sc.nextLine();
        System.out.print("Create a password (8+ chars, capital, number, special): ");
        String password = sc.nextLine();
        System.out.print("Enter your SA cell phone number (e.g. +27838968976): ");
        String cell = sc.nextLine();

        System.out.println(login.registerUser(username, password, cell));

        if (!login.isRegistered()) {
            System.out.println("Registration failed. Please restart the app.");
            sc.close();
            return;
        }

        System.out.println("\n=== LOGIN ===");
        System.out.print("Username: ");
        String loginUser = sc.nextLine();
        System.out.print("Password: ");
        String loginPass = sc.nextLine();
        System.out.println(login.returnLoginStatus(loginUser, loginPass));

        if (!login.loginUser(loginUser, loginPass)) {
            System.out.println("Access denied. You must log in to send messages.");
            sc.close();
            return;
        }

        System.out.println("Welcome to QuickChat.");

        // read any previously stored messages from the JSON file into the array
        Message.readStoredMessagesFromFile();

        int choice;
        do {
            System.out.println("\nPlease choose an option:");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            System.out.print("Enter your choice: ");
            choice = Integer.parseInt(sc.nextLine());

            if (choice == 1) {
                totalSent += sendMessages(sc);
            } else if (choice == 2) {
                String sentReport = Message.displaySentReport();
                if (sentReport.isEmpty()) {
                    System.out.println("No messages have been sent yet.");
                } else {
                    System.out.println(sentReport);
                }
            } else if (choice == 3) {
                System.out.println("Total messages sent: " + totalSent);
                System.out.println("Thank you for using QuickChat. Goodbye!");
            } else if (choice == 4) {
                storedMessagesMenu(sc, firstName);
            } else {
                System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 3);

        sc.close();
    }

    // fourth main menu option: Stored Messages
    private static void storedMessagesMenu(Scanner sc, String sender) {
        int option;
        do {
            System.out.println("\n=== STORED MESSAGES ===");
            System.out.println("a) Display sender and recipient of all stored messages");
            System.out.println("b) Display the longest stored message");
            System.out.println("c) Search for a message ID");
            System.out.println("d) Search for all messages for a particular recipient");
            System.out.println("e) Delete a message using the message hash");
            System.out.println("f) Display a report of all stored messages");
            System.out.println("g) Back to main menu");
            System.out.print("Enter your choice: ");
            String input = sc.nextLine().trim().toLowerCase();
            option = input.isEmpty() ? '?' : input.charAt(0);

            if (option == 'a') {
                System.out.println(Message.displaySenderAndRecipient(sender));
            } else if (option == 'b') {
                System.out.println(Message.displayLongestMessage());
            } else if (option == 'c') {
                System.out.print("Enter the message ID: ");
                System.out.println(Message.searchByMessageID(sc.nextLine().trim()));
            } else if (option == 'd') {
                System.out.print("Enter the recipient number: ");
                java.util.List<String> found = Message.searchByRecipient(sc.nextLine().trim());
                if (found.isEmpty()) {
                    System.out.println("No messages found for that recipient.");
                } else {
                    for (String m : found) {
                        System.out.println("\"" + m + "\"");
                    }
                }
            } else if (option == 'e') {
                System.out.print("Enter the message hash: ");
                System.out.println(Message.deleteByHash(sc.nextLine().trim()));
            } else if (option == 'f') {
                System.out.println(Message.displayStoredReport());
            } else if (option != 'g') {
                System.out.println("Invalid option. Please try again.");
            }
        } while (option != 'g');
    }

    private static int sendMessages(Scanner sc) {
        System.out.print("How many messages would you like to send? ");
        int numMessages = Integer.parseInt(sc.nextLine());
        int sent = 0;

        for (int i = 0; i < numMessages; i++) {
            Message msg = new Message();
            msg.setMessageID(Message.generateMessageID());

            System.out.print("Enter recipient cell number (with international code): ");
            msg.setRecipient(sc.nextLine());
            if (!msg.checkRecipientCell()) {
                System.out.println("Cell phone number is incorrectly formatted or "
                        + "does not contain an international code. Please correct "
                        + "the number and try again.");
                i--;
                continue;
            }

            System.out.print("Enter your message (max 250 characters): ");
            msg.setMessage(sc.nextLine());
            String lengthCheck = msg.checkMessageLength();
            System.out.println(lengthCheck);
            if (!lengthCheck.equals("Message ready to send.")) {
                i--;
                continue;
            }

            System.out.println("Choose an option:");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            int option = Integer.parseInt(sc.nextLine());
            String result = msg.sendMessage(option);
            System.out.println(result);

            if (option == 1 || option == 3) {
                sent++;
                System.out.println("\nFull message details:");
                System.out.println("Message ID: " + msg.getMessageID());
                System.out.println("Message Hash: " + msg.getMessageHash());
                System.out.println("Recipient: " + msg.getRecipient());
                System.out.println("Message: " + msg.getMessage());
            }
        }
        return sent;
    }
}
