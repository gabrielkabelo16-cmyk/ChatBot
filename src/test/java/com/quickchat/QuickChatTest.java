package com.quickchat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class QuickChatTest {

    // ================= PART 1: REGISTRATION AND LOGIN =================

    @Test
    void testUsernameCorrectlyFormatted() {
        Login login = new Login("Kyle", "Smith");
        String result = login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertEquals("Welcome Kyle, Smith it is great to see you.", result);
    }

    @Test
    void testUsernameIncorrectlyFormatted() {
        Login login = new Login("Kyle", "Smith");
        String result = login.registerUser("kyle!!!!!!!", "Ch&&sec@ke99!", "+27838968976");
        assertEquals("Username is not correctly formatted; please ensure that your "
                + "username contains an underscore and is no more than five "
                + "characters in length.", result);
    }

    @Test
    void testPasswordMeetsComplexity() {
        Login login = new Login("Kyle", "Smith");
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertTrue(login.checkPasswordComplexity());
    }

    @Test
    void testPasswordDoesNotMeetComplexity() {
        Login login = new Login("Kyle", "Smith");
        String result = login.registerUser("kyl_1", "password", "+27838968976");
        assertEquals("Password is not correctly formatted; please ensure that the "
                + "password contains at least eight characters, a capital letter, "
                + "a number, and a special character.", result);
    }

    @Test
    void testCellPhoneCorrectlyFormatted() {
        Login login = new Login("Kyle", "Smith");
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertTrue(login.checkCellPhoneNumber());
    }

    @Test
    void testCellPhoneIncorrectlyFormatted() {
        Login login = new Login("Kyle", "Smith");
        String result = login.registerUser("kyl_1", "Ch&&sec@ke99!", "08966553");
        assertEquals("Cell number is incorrectly formatted or does not contain an "
                + "international code; please correct the number and try again.", result);
    }

    @Test
    void testLoginSuccessful() {
        Login login = new Login("Kyle", "Smith");
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertTrue(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
    }

    @Test
    void testLoginFailed() {
        Login login = new Login("Kyle", "Smith");
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertFalse(login.loginUser("kyl_1", "wrongpass"));
    }

    @Test
    void testUsernameCorrectlyFormattedTrue() {
        Login login = new Login("Kyle", "Smith");
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertTrue(login.checkUserName());
    }

    @Test
    void testUsernameIncorrectlyFormattedFalse() {
        Login login = new Login("Kyle", "Smith");
        login.registerUser("kyle!!!!!!!", "Ch&&sec@ke99!", "+27838968976");
        assertFalse(login.checkUserName());
    }

    @Test
    void testPasswordMeetsComplexityTrue() {
        Login login = new Login("Kyle", "Smith");
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertTrue(login.checkPasswordComplexity());
    }

    @Test
    void testPasswordDoesNotMeetComplexityFalse() {
        Login login = new Login("Kyle", "Smith");
        login.registerUser("kyl_1", "password", "+27838968976");
        assertFalse(login.checkPasswordComplexity());
    }

    @Test
    void testCellPhoneCorrectlyFormattedTrue() {
        Login login = new Login("Kyle", "Smith");
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertTrue(login.checkCellPhoneNumber());
    }

    @Test
    void testCellPhoneIncorrectlyFormattedFalse() {
        Login login = new Login("Kyle", "Smith");
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "08966553");
        assertFalse(login.checkCellPhoneNumber());
    }

    // ================= PART 2: SENDING MESSAGES =================

    @Test
    void testMessageNotMoreThan250Characters() {
        // success case
        Message msg = new Message("1234567890", "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        assertEquals("Message ready to send.", msg.checkMessageLength());

        // failure case - 255 chars so it exceeds by 5
        String longMessage = "";
        for (int i = 0; i < 255; i++) {
            longMessage += "a";
        }
        Message msg2 = new Message("1234567890", "+27718693002", longMessage);
        assertEquals("Message exceeds 250 characters by 5; please reduce the size.",
                msg2.checkMessageLength());
    }

    @Test
    void testRecipientNumberCorrectlyFormatted() {
        // test data message 1 - +27718693002 is correct
        Message msg = new Message("1234567890", "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        assertTrue(msg.checkRecipientCell());

        // test data message 2 - 08575975889 has no international code
        Message msg2 = new Message("1234567890", "08575975889",
                "Hi Keegan, did you receive the payment?");
        assertFalse(msg2.checkRecipientCell());
    }

    @Test
    void testMessageHashIsCorrect() {
        // test the hashes in a loop like the task says
        String[] ids = {"0012345678", "0056789012"};
        String[] messages = {
            "Hi Mike, can you join us for dinner tonight?",
            "Hi Keegan, did you receive the payment?"
        };
        String[] expected = {"00:0:HITONIGHT?", "00:1:HIPAYMENT?"};

        for (int i = 0; i < messages.length; i++) {
            Message msg = new Message(ids[i], "+27718693002", messages[i]);
            msg.setNumMessagesSent(i + 1);
            assertEquals(expected[i], msg.createMessageHash());
        }
    }

    @Test
    void testMessageIDIsCreated() {
        String id = Message.generateMessageID();
        assertEquals(10, id.length());
    }

    @Test
    void testMessageSent() {
        Message msg = new Message("0012345678", "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");

        // 1) user selected 'Send Message'
        assertEquals("Message successfully sent.", msg.sendMessage(1));
        assertEquals(1, msg.returnTotalMessages());

        // 2) user selected 'Disregard Message'
        Message msg2 = new Message("0056789012", "+27718693002",
                "Hi Keegan, did you receive the payment?");
        assertEquals("Press 0 to delete the message.", msg2.sendMessage(2));
        assertEquals(0, msg2.returnTotalMessages());

        // 3) user selected 'Store Message'
        Message msg3 = new Message("0098765432", "+27718693002", "See you later");
        assertEquals("Message successfully stored.", msg3.sendMessage(3));
        assertEquals(1, msg3.returnTotalMessages());
    }

    @Test
    void testCheckMessageID() {
        Message msg = new Message("1234567890", "+27718693002", "Hello");
        assertTrue(msg.checkMessageID());

        Message msg2 = new Message("12345678901234567890", "+27718693002", "Hello");
        assertFalse(msg2.checkMessageID());
    }

    // ================= PART 3: ARRAYS, SEARCH, DELETE AND REPORT =================

    // fills the arrays with the five test data messages from the task
    // flag: 1 = Sent, 2 = Disregard, 3 = Stored
    private void loadTestData() {
        Message.clearAll();
        Message.setStorageFile("target/test-messages.json");

        Message m1 = new Message("1000000001", "+27834557896", "Did you get the cake?");
        Message m2 = new Message("2000000002", "+27838884567",
                "Where are you? You are late! I have asked you to be on time.");
        Message m3 = new Message("3000000003", "+27834484567", "Yohooo, I am at your gate.");
        Message m4 = new Message("0838884567", "0838884567", "It is dinner time!");
        Message m5 = new Message("5000000005", "+27838884567", "Ok, I am leaving without you.");

        m1.sendMessage(1);
        m2.sendMessage(3);
        m3.sendMessage(2);
        m4.sendMessage(1);
        m5.sendMessage(3);
    }

    @BeforeEach
    void setUpPart3() {
        loadTestData();
    }

    @Test
    void testSentMessagesArrayCorrectlyPopulated() {
        List<Message> sent = Message.getSentMessages();
        assertEquals(2, sent.size());
        assertEquals("Did you get the cake?", sent.get(0).getMessage());
        assertEquals("It is dinner time!", sent.get(1).getMessage());
    }

    @Test
    void testDisregardedMessagesArrayCorrectlyPopulated() {
        List<Message> disregarded = Message.getDisregardedMessages();
        assertEquals(1, disregarded.size());
        assertEquals("Yohooo, I am at your gate.", disregarded.get(0).getMessage());
    }

    @Test
    void testStoredMessagesArrayPopulatedFromJsonFile() {
        // wipe the array, then read it back from the JSON file
        Message.getStoredMessages().clear();
        Message.readStoredMessagesFromFile();
        List<Message> stored = Message.getStoredMessages();
        assertEquals(2, stored.size());
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                stored.get(0).getMessage());
        assertEquals("Ok, I am leaving without you.", stored.get(1).getMessage());
        assertEquals("+27838884567", stored.get(1).getRecipient());
    }

    @Test
    void testMessageHashAndIDArraysPopulated() {
        // sent (2) + stored (2) = 4; the disregarded message is not tracked
        assertEquals(4, Message.getMessageIDs().size());
        assertEquals(4, Message.getMessageHashes().size());
        assertTrue(Message.getMessageIDs().contains("0838884567"));
    }

    @Test
    void testDisplayLongestMessage() {
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                Message.displayLongestMessage());
    }

    @Test
    void testSearchForMessageID() {
        String result = Message.searchByMessageID("0838884567");
        assertTrue(result.contains("It is dinner time!"));
        assertEquals("Message ID not found.", Message.searchByMessageID("9999999999"));
    }

    @Test
    void testSearchMessagesForRecipient() {
        List<String> found = Message.searchByRecipient("+27838884567");
        assertEquals(2, found.size());
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                found.get(0));
        assertEquals("Ok, I am leaving without you.", found.get(1));
    }

    @Test
    void testDeleteMessageUsingHash() {
        String hash = Message.getStoredMessages().get(0).getMessageHash();
        assertEquals("Message \"Where are you? You are late! I have asked you to be on time.\""
                + " successfully deleted.", Message.deleteByHash(hash));
        assertEquals(1, Message.getStoredMessages().size());
        assertEquals("Message hash not found.", Message.deleteByHash(hash));
    }

    @Test
    void testDisplaySentReport() {
        String report = Message.displaySentReport();
        assertTrue(report.contains("Message Hash: "));
        assertTrue(report.contains("Recipient: +27834557896"));
        assertTrue(report.contains("Message: Did you get the cake?"));
        assertTrue(report.contains("Message: It is dinner time!"));
    }

    @Test
    void testDisplayStoredReport() {
        String report = Message.displayStoredReport();
        assertTrue(report.contains("Message ID: 2000000002"));
        assertTrue(report.contains("Message: Ok, I am leaving without you."));
    }
}
