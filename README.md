# QuickChat - POE Part 1, 2 & 3

Console chat app built for my POE. Java + Maven + JUnit.

## What it does

**Part 1 - Login/Registration (Login.java)**
- Register with a username (needs _ and max 5 chars), password (8+ chars,
  capital, number, special char) and SA cell number (needs international code)
- Login checks your details against what you registered with

**Part 2 - Messages (Message.java, QuickChat.java)**
- Only works after you log in
- Menu: 1) send messages 2) show recently sent messages 3) quit 4) stored messages
- You choose how many messages to send
- Each message gets a random 10-digit ID and a hash (like 00:0:HITHANKS)
- Messages are capped at 250 characters
- Can send, disregard (press 0) or store to a json file

**Part 3 - Arrays, Stored Messages and Report (Message.java, QuickChat.java)**
- Arrays filled while the app runs (nothing hard-coded): Sent Messages,
  Disregarded Messages, Stored Messages, Message Hash and Message ID
- Stored messages are saved to `messages.json` as a JSON array and read back
  into the Stored Messages array when the app starts
- Main menu option 4 (Stored Messages) lets you:
  - a) display the sender and recipient of all stored messages
  - b) display the longest stored message
  - c) search for a message ID and show the recipient and message
  - d) search for all messages for a particular recipient
  - e) delete a message using its message hash
  - f) display a report with the full details of all stored messages
- Unit tests use the five test data messages from the task (sent array,
  longest message, search by ID, search by recipient, delete by hash, report)

## Run it

```
mvn compile exec:java -Dexec.mainClass=com.quickchat.QuickChat
```

## Run the tests

```
mvn test
```

Tests run automatically on GitHub (see Actions tab) whenever code is pushed.

## References

- Cell number regex: stackoverflow.com/questions/19605444
- JUnit: junit.org/junit5/docs/current/user-guide/
- Storing messages in JSON: used FileWriter (researched gson/json-simple too)
