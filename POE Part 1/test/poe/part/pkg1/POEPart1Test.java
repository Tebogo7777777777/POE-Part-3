package poe.part.pkg1;

import java.util.*;
import javax.swing.*;
import java.time.LocalDateTime;
import java.io.*;
import java.nio.charset.StandardCharsets;// OpenAI,2025
import java.security.MessageDigest;// OpenAI,2025
import java.security.NoSuchAlgorithmException;// OpenAI,2025
import org.json.*;// OpenAI,2025

public class POEPart1 {
    // Data
    private static HashMap<String, String> registeredUsers = new HashMap<>();
    private static HashMap<String, String> userPhones = new HashMap<>();
    private static List<Message> messages = new ArrayList<>();
    private static List<Message> drafts = new ArrayList<>();
    private static String currentUser = null;
    private static final String MESSAGES_FILE = "messages.ser";
    private static final String STORED_FILE = "stored_messages.json";
    
    // Arrays for POE part 3
    private static List<Message> sentMessages = new ArrayList<>();
    private static List<Message> disregardedMessages = new ArrayList<>();
    private static List<Message> storedMessages = new ArrayList<>();
    private static List<String> messageHashes = new ArrayList<>();
    private static List<String> messageIDs = new ArrayList<>();

    // Message class with additional fields for Part 3
    private static class Message implements Serializable {
        String to;
        String content;
        String timestamp;
        String sender;
        String flag; // Added for POE Part 3
        String messageID; // Added for  POE Part 3
        String messageHash; // Added for POE Part 3

        public Message(String to, String content, String timestamp, String sender, String flag) {
            this.to = to;
            this.content = content;
            this.timestamp = timestamp;
            this.sender = sender;
            this.flag = flag;
            this.messageID = generateMessageID();
            this.messageHash = generateMessageHash();
        }

        private String generateMessageID() {
            return UUID.randomUUID().toString().substring(0, 8);
        }

        private String generateMessageHash() {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest((content + timestamp).getBytes(StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                return hexString.toString().substring(0, 16);
            } catch (NoSuchAlgorithmException e) {
                return "hash-error";
            }
        }

        @Override
        public String toString() {
            return "From: " + sender + " | To: " + to +
                   "\nMessage: " + content + "\nTime: " + timestamp + 
                   "\nStatus: " + flag + "\nID: " + messageID + "\nHash: " + messageHash + "\n";
        }
    }

    public static void main(String[] args) {
        loadMessagesFromFile();
        loadStoredMessages();
        populateTestData(); // Add test data for Part 3
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.println("\n===== USER CHAT APP =====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.println("4. Part 3 Features (Demo)"); // Added for Part 3
            System.out.print("Choose: ");

            String choice = scanner.nextLine();
            // Case to select 
            switch (choice) {
                case "1":
                    registerNewUser(scanner);
                    break;
                case "2":
                    loginUser(scanner);
                    break;
                case "3":
                    exit = true;
                    saveMessagesToFile();
                    saveStoredMessages();
                    System.out.println("Goodbye!");
                    break;
                case "4": // Added for Part 3
                    demoPart3Features();
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
        scanner.close();
    }

    // Part 3: Demo features
    private static void demoPart3Features() {
        boolean keepGoing = true;
        while (keepGoing) {
            String choice = JOptionPane.showInputDialog(null,
                "Part 3 Features:\n1. Display sender/recipient of all sent messages\n" +
                "2. Display longest sent message\n3. Search by message ID\n" +
                "4. Search messages by recipient\n5. Delete by message hash\n" +
                "6. Display full report\n7. Back to main menu",
                "Part 3 Features", JOptionPane.QUESTION_MESSAGE);

            if (choice == null) break;
            // case inserted for choices
            switch (choice) {
                case "1": displaySendersAndRecipients(); break;
                case "2": displayLongestMessage(); break;
                case "3": searchByMessageID(); break;
                case "4": searchByRecipient(); break;
                case "5": deleteByMessageHash(); break;
                case "6": displayFullReport(); break;
                case "7": keepGoing = false; break;
                default: JOptionPane.showMessageDialog(null, "Invalid option.");
            }
        }
    }

    // Implemented for part 3 This displays senders and recievers message
    private static void displaySendersAndRecipients() {
        StringBuilder sb = new StringBuilder("Senders and Recipients of Sent Messages:\n");
        for (Message msg : sentMessages) {
            sb.append("From: ").append(msg.sender).append(" | To: ").append(msg.to).append("\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }
// This is to show if the is no message that has been send 
    private static void displayLongestMessage() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No sent messages.");
            return;
        }

        Message longest = sentMessages.get(0);
        for (Message msg : sentMessages) {
            if (msg.content.length() > longest.content.length()) {
                longest = msg;
            }
        }
        JOptionPane.showMessageDialog(null, "Longest message:\n" + longest.content);
    }

    private static void searchByMessageID() {
        String id = JOptionPane.showInputDialog("Enter message ID to search:");
        if (id == null || id.isEmpty()) return;

        for (Message msg : sentMessages) {
            if (msg.messageID.equals(id)) {
                JOptionPane.showMessageDialog(null, "Message found:\n" + 
                    "To: " + msg.to + "\nMessage: " + msg.content);
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Message with ID " + id + " not found.");
    }

    private static void searchByRecipient() {
        String recipient = JOptionPane.showInputDialog("Enter recipient to search:");
        if (recipient == null || recipient.isEmpty()) return;

        StringBuilder sb = new StringBuilder("Messages to " + recipient + ":\n");
        boolean found = false;
        for (Message msg : sentMessages) {
            if (msg.to.equals(recipient)) {
                sb.append("From: ").append(msg.sender).append(": ").append(msg.content).append("\n\n");
                found = true;
            }
        }
        if (!found) sb.append("No messages found for this recipient.");
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    private static void deleteByMessageHash() {
        String hash = JOptionPane.showInputDialog("Enter message hash to delete:");
        if (hash == null || hash.isEmpty()) return;

        for (int i = 0; i < sentMessages.size(); i++) {
            if (sentMessages.get(i).messageHash.equals(hash)) {
                sentMessages.remove(i);
                JOptionPane.showMessageDialog(null, "Message deleted successfully.");
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Message with hash " + hash + " not found.");
    }

    private static void displayFullReport() {
        StringBuilder sb = new StringBuilder("FULL MESSAGE REPORT\n\n");
        sb.append("=== SENT MESSAGES ===\n");
        for (Message msg : sentMessages) {
            sb.append(msg.toString()).append("--------------------\n");
        }
        
        sb.append("\n=== DISREGARDED MESSAGES ===\n");
        for (Message msg : disregardedMessages) {
            sb.append(msg.toString()).append("--------------------\n");
        }
        
        sb.append("\n=== STORED MESSAGES ===\n");
        for (Message msg : storedMessages) {
            sb.append(msg.toString()).append("--------------------\n");
        }
        
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    // Part 3: Data management
    private static void populateTestData() {
        // Test Data Message 1
        Message msg1 = new Message("+27834557896", "Did you get the cake?", 
            LocalDateTime.now().toString(), "testUser1", "Sent");
        sentMessages.add(msg1);
        messageHashes.add(msg1.messageHash);
        messageIDs.add(msg1.messageID);

        // Test Data Message 2
        Message msg2 = new Message("+27838884567", "Where are you? You are late! I have asked you to be on time.", 
            LocalDateTime.now().toString(), "testUser2", "Stored");
        storedMessages.add(msg2);
        messageHashes.add(msg2.messageHash);
        messageIDs.add(msg2.messageID);

        // Test Data Message 3
        Message msg3 = new Message("+27834484567", "Yohoooo, I am at your gate.", 
            LocalDateTime.now().toString(), "testUser3", "Disregard");
        disregardedMessages.add(msg3);
        messageHashes.add(msg3.messageHash);
        messageIDs.add(msg3.messageID);

        // Test Data Message 4
        Message msg4 = new Message("0838884567", "It is dinner time !", 
            LocalDateTime.now().toString(), "testUser4", "Sent");
        sentMessages.add(msg4);
        messageHashes.add(msg4.messageHash);
        messageIDs.add(msg4.messageID);

        // Test Data Message 5
        Message msg5 = new Message("+27838884567", "Ok, I am leaving without you.", 
            LocalDateTime.now().toString(), "testUser5", "Stored");
        storedMessages.add(msg5);
        messageHashes.add(msg5.messageHash);
        messageIDs.add(msg5.messageID);
    }

    private static void loadStoredMessages() {
        File file = new File(STORED_FILE);
        if (!file.exists()) {
            System.out.println("No stored messages file found, starting fresh.");
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            String jsonContent = scanner.useDelimiter("\\Z").next();
            JSONArray jsonArray = new JSONArray(jsonContent);
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonMsg = jsonArray.getJSONObject(i);
                Message msg = new Message(
                    jsonMsg.getString("to"),
                    jsonMsg.getString("content"),
                    jsonMsg.getString("timestamp"),
                    jsonMsg.getString("sender"),
                    jsonMsg.getString("flag")
                );
                storedMessages.add(msg);
                messageHashes.add(msg.messageHash);
                messageIDs.add(msg.messageID);
            }
        } catch (Exception e) {
            System.out.println("Error loading stored messages: " + e.getMessage());
        }
    }
    // (OpenAI,2025 )
    // This is JSon that stores the messages
    private static void saveStoredMessages() {
        JSONArray jsonArray = new JSONArray();
        for (Message msg : storedMessages) {
            JSONObject jsonMsg = new JSONObject();
            jsonMsg.put("to", msg.to);
            jsonMsg.put("content", msg.content);
            jsonMsg.put("timestamp", msg.timestamp);
            jsonMsg.put("sender", msg.sender);
            jsonMsg.put("flag", msg.flag);
            jsonMsg.put("messageID", msg.messageID);
            jsonMsg.put("messageHash", msg.messageHash);
            jsonArray.put(jsonMsg);
        }

        try (FileWriter file = new FileWriter(STORED_FILE)) {
            file.write(jsonArray.toString());
            file.flush();
        } catch (IOException e) {
            System.out.println("Error saving stored messages: " + e.getMessage());
        }
    }

    // Registration for the chat app
    private static void registerNewUser(Scanner scanner) {
        String username, password, phone;
    // This Checks if the user name is less than 5 charecters and it contain a underscore 
        while (true) {
            System.out.print("Username (underscore & ≤5 chars): ");
            username = scanner.nextLine();

            if (!checkUserName(username)) {
                System.out.println("Invalid format.");
            } else if (registeredUsers.containsKey(username)) {
                System.out.println("Username taken.");
            } else {
                break;
            }
        }

        while (true) {
            System.out.print("Password (8+ characters, 1 uppercase, 1 number, 1 special): ");
            password = scanner.nextLine();
            if (checkPasswordComplexity(password)) break;
            System.out.println("Weak password.");
        }

        while (true) {
            System.out.print("Phone (+27 and up to 10 digits): ");
            phone = scanner.nextLine();
            if (checkCellPhoneNumber(phone)) break;
            System.out.println("Cell phone number is incorrectly formatted.");
        }

        registeredUsers.put(username, password);
        userPhones.put(username, phone);
        System.out.println("User registered.");
    }

    // Login and checks if the user name and password is the same one as the ones the user registered
    private static void loginUser(Scanner scanner) {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (loginUser(username, password)) {
            currentUser = username;
            JOptionPane.showMessageDialog(null, "Welcome to QuickChat " + username + "!");
            openChatMenu();
        } else {
            System.out.println("Login failed.");
        }
    }

    private static boolean loginUser(String username, String password) {
        return registeredUsers.containsKey(username) && registeredUsers.get(username).equals(password);
    }

    private static void openChatMenu() {
        boolean keepGoing = true;
        while (keepGoing) {
            String choice = JOptionPane.showInputDialog(null,
                "Welcome to QuickChat Menu:\n1. Write Draft\n2. View Messages\n3. View Drafts\n4. Send Draft\n5. Delete Draft\n6. Exit",
                "QuickChat", JOptionPane.QUESTION_MESSAGE);

            if (choice == null) break;

            switch (choice) {
                case "1": writeDraft(); break;
                case "2": showMessages(); break;
                case "3": showDrafts(); break;
                case "4": sendDraft(); break;
                case "5": deleteDraft(); break;
                case "6":
                    JOptionPane.showMessageDialog(null, "Goodbye.");
                    keepGoing = false;
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Invalid option.");
            }
        }
        saveMessagesToFile();
    }

    // Messaging features
    private static void writeDraft() {
        String to = JOptionPane.showInputDialog("Send to:");
        String content = JOptionPane.showInputDialog("Message:");

        if (to != null && content != null && !to.isEmpty() && !content.isEmpty()) {
            drafts.add(new Message(to, content, LocalDateTime.now().toString(), currentUser, "Draft"));
            JOptionPane.showMessageDialog(null, "Saved as draft.");
        }
    }

    private static void showDrafts() {
        if (drafts.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No drafts.");
            return;
        }

        StringBuilder out = new StringBuilder("Your Drafts:\n");
        for (int i = 0; i < drafts.size(); i++) {
            Message d = drafts.get(i);
            out.append(i).append(") To: ").append(d.to).append(" - ").append(d.content).append("\n");
        }

        JOptionPane.showMessageDialog(null, out.toString());
    }

    private static void sendDraft() {
        if (drafts.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No drafts to send.");
            return;
        }

        String input = JOptionPane.showInputDialog("Enter draft number to send:");
        try {
            int idx = Integer.parseInt(input);
            if (idx >= 0 && idx < drafts.size()) {
                Message sentMsg = drafts.remove(idx);
                sentMsg.flag = "Sent";
                messages.add(sentMsg);
                sentMessages.add(sentMsg);
                messageHashes.add(sentMsg.messageHash);
                messageIDs.add(sentMsg.messageID);
                JOptionPane.showMessageDialog(null, "Draft sent.");
            } else {
                JOptionPane.showMessageDialog(null, "Invalid number.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid input.");
        }
    }

    private static void deleteDraft() {
        if (drafts.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No drafts to delete.");
            return;
        }

        String input = JOptionPane.showInputDialog("Enter draft number to delete:");
        try {
            int idx = Integer.parseInt(input);
            if (idx >= 0 && idx < drafts.size()) {
                drafts.remove(idx);
                JOptionPane.showMessageDialog(null, "Draft deleted.");
            } else {
                JOptionPane.showMessageDialog(null, "Invalid number.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid input.");
        }
    }

    private static void showMessages() {
        StringBuilder out = new StringBuilder("Messages:\n");
        boolean found = false;

        for (Message msg : messages) {
            if (msg.sender.equals(currentUser) || msg.to.equals(currentUser)) {
                out.append(msg.toString()).append("--------------------\n");
                found = true;
            }
        }

        if (!found) out.append("No messages.");
        JOptionPane.showMessageDialog(null, out.toString());
    }

    // Validations
    public static boolean checkUserName(String username) {
        return username != null && username.length() <= 5 && username.contains("_");
    }

    public static boolean checkPasswordComplexity(String password) {
        if (password == null || password.length() < 8) return false;

        boolean upper = false, digit = false, special = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) upper = true;
            else if (Character.isDigit(c)) digit = true;
            else if (!Character.isLetterOrDigit(c)) special = true;
        }

        return upper && digit && special;
    }

    public static boolean checkCellPhoneNumber(String phone) {
        if (phone == null || !phone.startsWith("+")) return false;

        String digits = phone.substring(1).replaceAll("\\D", "");
        int codeLength = Math.min(3, digits.length() - 1);
        return (digits.length() - codeLength) <= 10;
    }

    // Persistence
    @SuppressWarnings("unchecked")
    private static void loadMessagesFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(MESSAGES_FILE))) {
            messages = (List<Message>) ois.readObject();
            System.out.println("Messages loaded: " + messages.size());
        } catch (FileNotFoundException e) {
            System.out.println("No saved messages found.");
        } catch (Exception e) {
            System.out.println("Error loading messages: " + e.getMessage());
        }
    }

    private static void saveMessagesToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MESSAGES_FILE))) {
            oos.writeObject(messages);
            System.out.println("Messages saved: " + messages.size());
        } catch (IOException e) {
            System.out.println("Error saving messages.");
        }
    }
}