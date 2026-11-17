package com.quickchat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Message {

    private String messageID;
    private int numMessagesSent;
    private String recipient;
    private String message;
    private String messageHash;

    // ---- Part 3 arrays (filled while the program runs, nothing hard-coded) ----
    private static List<Message> sentMessages = new ArrayList<>();
    private static List<Message> disregardedMessages = new ArrayList<>();
    private static List<Message> storedMessages = new ArrayList<>();
    private static List<String> messageHashes = new ArrayList<>();
    private static List<String> messageIDs = new ArrayList<>();

    // file the stored messages are written to / read from
    private static String storageFile = "messages.json";

    public Message() {
    }

    public Message(String messageID, String recipient, String message) {
        this.messageID = messageID;
        this.recipient = recipient;
        this.message = message;
    }

    // message ID must be 10 chars or less
    public boolean checkMessageID() {
        if (messageID.length() <= 10) {
            return true;
        }
        return false;
    }

    // reuse the same check as Login - number needs international code
    public boolean checkRecipientCell() {
        if (recipient.matches("^\\+[1-9]\\d{9,13}$")) {
            return true;
        }
        return false;
    }

    // hash = first 2 digits of ID : message number : first+last word (caps)
    // e.g. 00:0:HITHANKS
    public String createMessageHash() {
        String firstTwo = messageID.substring(0, 2);
        String[] words = message.trim().split(" ");
        String firstWord = words[0];
        String lastWord = words[words.length - 1];
        String hash = firstTwo + ":" + (numMessagesSent - 1) + ":" + firstWord + lastWord;
        return hash.toUpperCase();
    }

    // user picks: 1 = send, 2 = disregard, 3 = store
    public String sendMessage(int choice) {
        if (choice == 1) {
            numMessagesSent++;
            messageHash = createMessageHash();
            sentMessages.add(this);
            recordIDAndHash();
            return "Message successfully sent.";
        } else if (choice == 2) {
            disregardedMessages.add(this);
            return "Press 0 to delete the message.";
        } else if (choice == 3) {
            numMessagesSent++;
            messageHash = createMessageHash();
            storedMessages.add(this);
            recordIDAndHash();
            writeStoredMessagesToFile();
            return "Message successfully stored.";
        } else {
            return "Invalid option.";
        }
    }

    // keeps the Message ID and Message Hash arrays in step with the messages
    private void recordIDAndHash() {
        messageIDs.add(messageID);
        messageHashes.add(messageHash);
    }

    // checks the 250 character limit
    public String checkMessageLength() {
        if (message.length() > 250) {
            int extra = message.length() - 250;
            return "Message exceeds 250 characters by " + extra
                    + "; please reduce the size.";
        }
        return "Message ready to send.";
    }

    // prints all messages sent while the program is running
    public String printMessages() {
        String all = "";
        for (Message m : sentMessages) {
            all += "Message ID: " + m.messageID + "\n";
            all += "Message Hash: " + m.messageHash + "\n";
            all += "Recipient: " + m.recipient + "\n";
            all += "Message: " + m.message + "\n\n";
        }
        return all;
    }

    public int returnTotalMessages() {
        return numMessagesSent;
    }

    // ---------------- JSON storage ----------------

    // escapes a value so it is safe inside a JSON string
    private static String jsonEscape(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"') {
                sb.append("\\\"");
            } else if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\t') {
                sb.append("\\t");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // turns this message into a JSON object string
    private String toJson() {
        return "  {\n"
                + "    \"messageID\": \"" + jsonEscape(messageID) + "\",\n"
                + "    \"messageHash\": \"" + jsonEscape(messageHash == null ? "" : messageHash) + "\",\n"
                + "    \"recipient\": \"" + jsonEscape(recipient) + "\",\n"
                + "    \"message\": \"" + jsonEscape(message) + "\"\n"
                + "  }";
    }

    // writes the whole stored messages array to the json file as a JSON array
    public static void writeStoredMessagesToFile() {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < storedMessages.size(); i++) {
            sb.append(storedMessages.get(i).toJson());
            if (i < storedMessages.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]\n");
        try (FileWriter fw = new FileWriter(storageFile, false)) {
            fw.write(sb.toString());
        } catch (IOException e) {
            System.out.println("Could not store message.");
        }
    }

    // reads the JSON file back into the Stored Messages array
    // (small hand-written reader so no extra libraries are needed)
    public static void readStoredMessagesFromFile() {
        storedMessages.clear();
        File file = new File(storageFile);
        if (!file.exists()) {
            return;
        }
        String content;
        try {
            content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Could not read stored messages.");
            return;
        }

        String[] current = new String[4]; // id, hash, recipient, message
        boolean inObject = false;
        int i = 0;
        while (i < content.length()) {
            char c = content.charAt(i);
            if (c == '{') {
                inObject = true;
                current = new String[4];
                i++;
            } else if (c == '}') {
                if (inObject && current[0] != null) {
                    Message m = new Message(current[0], current[2], current[3]);
                    m.messageHash = current[1];
                    m.numMessagesSent = 1;
                    storedMessages.add(m);
                }
                inObject = false;
                i++;
            } else if (inObject && c == '"') {
                // read key
                int[] end = new int[1];
                String key = readJsonString(content, i, end);
                i = end[0];
                // skip to the value's opening quote
                while (i < content.length() && content.charAt(i) != '"') {
                    i++;
                }
                String value = readJsonString(content, i, end);
                i = end[0];
                if (key.equals("messageID")) {
                    current[0] = value;
                } else if (key.equals("messageHash")) {
                    current[1] = value;
                } else if (key.equals("recipient")) {
                    current[2] = value;
                } else if (key.equals("message")) {
                    current[3] = value;
                }
            } else {
                i++;
            }
        }
    }

    // reads one JSON string starting at the opening quote at position start;
    // end[0] is set to the position just after the closing quote
    private static String readJsonString(String text, int start, int[] end) {
        StringBuilder sb = new StringBuilder();
        int i = start + 1;
        while (i < text.length() && text.charAt(i) != '"') {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char n = text.charAt(i + 1);
                if (n == 'n') {
                    sb.append('\n');
                } else if (n == 'r') {
                    sb.append('\r');
                } else if (n == 't') {
                    sb.append('\t');
                } else {
                    sb.append(n);
                }
                i += 2;
            } else {
                sb.append(c);
                i++;
            }
        }
        end[0] = i + 1;
        return sb.toString();
    }

    public static void setStorageFile(String path) {
        storageFile = path;
    }

    // ---------------- Part 3: array features ----------------

    // 2a. sender and recipient of all stored messages
    public static String displaySenderAndRecipient(String sender) {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        String all = "";
        for (Message m : storedMessages) {
            all += "Sender: " + sender + " | Recipient: " + m.recipient + "\n";
        }
        return all;
    }

    // 2b. the longest stored message
    public static String displayLongestMessage() {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        Message longest = storedMessages.get(0);
        for (Message m : storedMessages) {
            if (m.message.length() > longest.message.length()) {
                longest = m;
            }
        }
        return longest.message;
    }

    // 2c. search for a message ID - returns the recipient and message
    public static String searchByMessageID(String id) {
        for (int i = 0; i < messageIDs.size(); i++) {
            if (messageIDs.get(i).equals(id)) {
                Message m = findByID(id);
                if (m != null) {
                    return "Recipient: " + m.recipient + "\nMessage: " + m.message;
                }
            }
        }
        return "Message ID not found.";
    }

    // looks through the sent and stored arrays for a message with this ID
    private static Message findByID(String id) {
        for (Message m : sentMessages) {
            if (m.messageID.equals(id)) {
                return m;
            }
        }
        for (Message m : storedMessages) {
            if (m.messageID.equals(id)) {
                return m;
            }
        }
        return null;
    }

    // 2d. all the messages sent or stored for one recipient
    public static List<String> searchByRecipient(String recipient) {
        List<String> found = new ArrayList<>();
        for (Message m : sentMessages) {
            if (m.recipient.equals(recipient)) {
                found.add(m.message);
            }
        }
        for (Message m : storedMessages) {
            if (m.recipient.equals(recipient)) {
                found.add(m.message);
            }
        }
        return found;
    }

    // 2e. delete a message using its hash
    public static String deleteByHash(String hash) {
        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            if (hash.equals(m.messageHash)) {
                storedMessages.remove(i);
                messageIDs.remove(m.messageID);
                messageHashes.remove(hash);
                writeStoredMessagesToFile();
                return "Message \"" + m.message + "\" successfully deleted.";
            }
        }
        for (int i = 0; i < sentMessages.size(); i++) {
            Message m = sentMessages.get(i);
            if (hash.equals(m.messageHash)) {
                sentMessages.remove(i);
                messageIDs.remove(m.messageID);
                messageHashes.remove(hash);
                return "Message \"" + m.message + "\" successfully deleted.";
            }
        }
        return "Message hash not found.";
    }

    // 2f. report listing the full details of all stored messages
    public static String displayStoredReport() {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        String all = "";
        for (Message m : storedMessages) {
            all += "Message ID: " + m.messageID + "\n";
            all += "Message Hash: " + m.messageHash + "\n";
            all += "Recipient: " + m.recipient + "\n";
            all += "Message: " + m.message + "\n\n";
        }
        return all;
    }

    // report of every sent message: hash, recipient and message
    public static String displaySentReport() {
        String all = "";
        for (Message m : sentMessages) {
            all += "Message Hash: " + m.messageHash + "\n";
            all += "Recipient: " + m.recipient + "\n";
            all += "Message: " + m.message + "\n\n";
        }
        return all;
    }

    // getters for the arrays (used by the unit tests)
    public static List<Message> getSentMessages() {
        return sentMessages;
    }

    public static List<Message> getDisregardedMessages() {
        return disregardedMessages;
    }

    public static List<Message> getStoredMessages() {
        return storedMessages;
    }

    public static List<String> getMessageHashes() {
        return messageHashes;
    }

    public static List<String> getMessageIDs() {
        return messageIDs;
    }

    // empties all the arrays (used before each unit test)
    public static void clearAll() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
    }

    // random 10 digit message ID for tracking
    public static String generateMessageID() {
        Random rand = new Random();
        String id = "";
        for (int i = 0; i < 10; i++) {
            id += rand.nextInt(10);
        }
        return id;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public int getNumMessagesSent() {
        return numMessagesSent;
    }

    public void setNumMessagesSent(int numMessagesSent) {
        this.numMessagesSent = numMessagesSent;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageHash() {
        return messageHash;
    }
}
