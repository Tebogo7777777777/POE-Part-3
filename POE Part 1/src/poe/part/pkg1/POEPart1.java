package poe.part.pkg1;

import java.util.*;
import javax.swing.*;
import java.time.LocalDateTime;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.json.*;

public class POEPart1 {
    // Data
    private static HashMap<String, String> registeredUsers = new HashMap<>();
    private static HashMap<String, String> userPhones = new HashMap<>();
    private static List<Message> messages = new ArrayList<>();
    private static List<Message> drafts = new ArrayList<>();
    private static String currentUser = null;
    private static final String MESSAGES_FILE = "messages.ser";
    private static final String STORED_FILE = "stored_messages.json";
    
    // Arrays for Part 3
    private static List<Message> sentMessages = new ArrayList<>();
    private static List<Message> disregardedMessages = new ArrayList<>();
    private static List<Message> storedMessages = new ArrayList<>();
    private static List<String> messageHashes = new ArrayList<>();
    private static List<String> messageIDs = new ArrayList<>();

    // Message class with additional fields for Part 3
    private static class Message implements Serializable {
        private static final long serialVersionUID = 1L;
        String to;
        String content;
        String timestamp;
        String sender;
        String flag;
        String messageID;
        String messageHash;

        public Message(String to, String content, String timestamp, String sender, String flag) {
            this.to = to;
            this.content = content;
            this.timestamp = timestamp;
            this.sender = sender;
            this.flag = flag;
            this.messageID = generateMessageID();
            this.messageHash = generateMessageHash();
        }

        // Constructor for loading from JSON with existing ID and hash
        public Message(String to, String content, String timestamp, String sender, String flag, String messageID, String messageHash) {
            this.to = to;
            this.content = content;
            this.timestamp = timestamp;
            this.sender = sender;
            this.flag = flag;
            this.messageID = messageID;
            this.messageHash = messageHash;
        }

        private String generateMessageID() {
            return UUID.randomUUID().toString().substring(0, 8);
        }

        private String generateMessageHash() {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                String input = content + timestamp + sender + to;
                byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
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
        populateTestData();
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.println("\n===== USER CHAT APP =====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.println("4. Part 3 Features (Demo)");
            System.out.print("Choose: ");

            String choice = scanner.nextLine();

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
                case "4":
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
                "Part 3 Features:\n" +
                "1. Display sender/recipient of all sent messages\n" +
                "2. Display longest sent message\n" +
                "3. Search by message ID\n" +
                "4. Search messages by recipient\n" +
                "5. Delete by message hash\n" +
                "6. Display full report\n" +
                "7. Back to main menu",
                "Part 3 Features", JOptionPane.QUESTION_MESSAGE);

            if (choice == null) break;

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

    // Part 3: Feature implementations
    private static void displaySendersAndRecipients() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No sent messages found.");
            return;
        }

        StringBuilder sb = new StringBuilder("Senders and Recipients of Sent Messages:\n\n");
        for (Message msg : sentMessages) {
            sb.append("From: ").append(msg.sender).append(" | To: ").append(msg.to).append("\n");
        }
        
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(400, 300));
        JOptionPane.showMessageDialog(null, scrollPane, "Senders and Recipients", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void displayLongestMessage() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No sent messages found.");
            return;
        }

        Message longest = sentMessages.get(0);
        for (Message msg : sentMessages) {
            if (msg.content.length() > longest.content.length()) {
                longest = msg;
            }
        }
        
        String result = "Longest message (" + longest.content.length() + " characters):\n\n" +
                       "From: " + longest.sender + "\n" +
                       "To: " + longest.to + "\n" +
                       "Message: " + longest.content + "\n" +
                       "Time: " + longest.timestamp;
        
        JTextArea textArea = new JTextArea(result);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(400, 300));
        JOptionPane.showMessageDialog(null, scrollPane, "Longest Message", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void searchByMessageID() {
        String id = JOptionPane.showInputDialog("Enter message ID to search:");
        if (id == null || id.trim().isEmpty()) return;

        // Search in all message lists
        List<Message> allMessages = new ArrayList<>();
        allMessages.addAll(sentMessages);
        allMessages.addAll(disregardedMessages);
        allMessages.addAll(storedMessages);

        for (Message msg : allMessages) {
            if (msg.messageID.equals(id.trim())) {
                String result = "Message found:\n\n" +
                               "From: " + msg.sender + "\n" +
                               "To: " + msg.to + "\n" +
                               "Message: " + msg.content + "\n" +
                               "Status: " + msg.flag + "\n" +
                               "Time: " + msg.timestamp + "\n" +
                               "Hash: " + msg.messageHash;
                
                JTextArea textArea = new JTextArea(result);
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new java.awt.Dimension(400, 300));
                JOptionPane.showMessageDialog(null, scrollPane, "Message Found", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Message with ID '" + id + "' not found.");
    }

    private static void searchByRecipient() {
        String recipient = JOptionPane.showInputDialog("Enter recipient to search:");
        if (recipient == null || recipient.trim().isEmpty()) return;

        StringBuilder sb = new StringBuilder("Messages to " + recipient + ":\n\n");
        boolean found = false;
        
        for (Message msg : sentMessages) {
            if (msg.to.equals(recipient.trim())) {
                sb.append("From: ").append(msg.sender).append("\n");
                sb.append("Message: ").append(msg.content).append("\n");
                sb.append("Time: ").append(msg.timestamp).append("\n");
                sb.append("ID: ").append(msg.messageID).append("\n\n");
                found = true;
            }
        }
        
        if (!found) sb.append("No messages found for recipient '").append(recipient).append("'.");
        
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(400, 300));
        JOptionPane.showMessageDialog(null, scrollPane, "Messages by Recipient", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void deleteByMessageHash() {
        String hash = JOptionPane.showInputDialog("Enter message hash to delete:");
        if (hash == null || hash.trim().isEmpty()) return;

        // Try to delete from sent messages
        for (int i = 0; i < sentMessages.size(); i++) {
            if (sentMessages.get(i).messageHash.equals(hash.trim())) {
                Message deleted = sentMessages.remove(i);
                messageHashes.remove(deleted.messageHash);
                messageIDs.remove(deleted.messageID);
                JOptionPane.showMessageDialog(null, "Message deleted successfully from sent messages.");
                return;
            }
        }

        // Try to delete from stored messages
        for (int i = 0; i < storedMessages.size(); i++) {
            if (storedMessages.get(i).messageHash.equals(hash.trim())) {
                Message deleted = storedMessages.remove(i);
                messageHashes.remove(deleted.messageHash);
                messageIDs.remove(deleted.messageID);
                JOptionPane.showMessageDialog(null, "Message deleted successfully from stored messages.");
                return;
            }
        }

        // Try to delete from disregarded messages
        for (int i = 0; i < disregardedMessages.size(); i++) {
            if (disregardedMessages.get(i).messageHash.equals(hash.trim())) {
                Message deleted = disregardedMessages.remove(i);
                messageHashes.remove(deleted.messageHash);
                messageIDs.remove(deleted.messageID);
                JOptionPane.showMessageDialog(null, "Message deleted successfully from disregarded messages.");
                return;
            }
        }

        JOptionPane.showMessageDialog(null, "Message with hash '" + hash + "' not found.");
    }

    private static void displayFullReport() {
        StringBuilder sb = new StringBuilder("FULL MESSAGE REPORT\n\n");
        
        sb.append("=== SENT MESSAGES (").append(sentMessages.size()).append(") ===\n");
        if (sentMessages.isEmpty()) {
            sb.append("No sent messages.\n");
        } else {
            for (Message msg : sentMessages) {
                sb.append(msg.toString()).append("--------------------\n");
            }
        }
        
        sb.append("\n=== DISREGARDED MESSAGES (").append(disregardedMessages.size()).append(") ===\n");
        if (disregardedMessages.isEmpty()) {
            sb.append("No disregarded messages.\n");
        } else {
            for (Message msg : disregardedMessages) {
                sb.append(msg.toString()).append("--------------------\n");
            }
        }
        
        sb.append("\n=== STORED MESSAGES (").append(storedMessages.size()).append(") ===\n");
        if (storedMessages.isEmpty()) {
            sb.append("No stored messages.\n");
        } else {
            for (Message msg : storedMessages) {
                sb.append(msg.toString()).append("--------------------\n");
            }
        }
        
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(500, 400));
        JOptionPane.showMessageDialog(null, scrollPane, "Full Report", JOptionPane.INFORMATION_MESSAGE);
    }

    // Part 3: Data management
    private static void populateTestData() {
        // Only populate if no existing data
        if (!sentMessages.isEmpty() || !storedMessages.isEmpty() || !disregardedMessages.isEmpty()) {
            return;
        }

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
                
                // Load with existing ID and hash if available
                String messageID = jsonMsg.optString("messageID", null);
                String messageHash = jsonMsg.optString("messageHash", null);
                
                Message msg;
                if (messageID != null && messageHash != null) {
                    msg = new Message(
                        jsonMsg.getString("to"),
                        jsonMsg.getString("content"),
                        jsonMsg.getString("timestamp"),
                        jsonMsg.getString("sender"),
                        jsonMsg.getString("flag"),
                        messageID,
                        messageHash
                    );
                } else {
                    msg = new Message(
                        jsonMsg.getString("to"),
                        jsonMsg.getString("content"),
                        jsonMsg.getString("timestamp"),
                        jsonMsg.getString("sender"),
                        jsonMsg.getString("flag")
                    );
                }
                
                // Add to appropriate list based on flag
                switch (msg.flag) {
                    case "Sent":
                        sentMessages.add(msg);
                        break;
                    case "Stored":
                        storedMessages.add(msg);
                        break;
                    case "Disregard":
                        disregardedMessages.add(msg);
                        break;
                }
                
                messageHashes.add(msg.messageHash);
                messageIDs.add(msg.messageID);
            }
            System.out.println("Stored messages loaded successfully.");
        } catch (Exception e) {
            System.out.println("Error loading stored messages: " + e.getMessage());
        }
    }

    private static void saveStoredMessages() {
        JSONArray jsonArray = new JSONArray();
        
        // Save all message types
        List<Message> allMessages = new ArrayList<>();
        allMessages.addAll(sentMessages);
        allMessages.addAll(storedMessages);
        allMessages.addAll(disregardedMessages);
        
        for (Message msg : allMessages) {
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
            file.write(jsonArray.toString(2)); // Pretty print with indentation
            file.flush();
            System.out.println("Stored messages saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving stored messages: " + e.getMessage());
        }
    }

    // Registration
    private static void registerNewUser(Scanner scanner) {
        String username, password, phone;

        while (true) {
            System.out.print("Username (underscore & ≤5 chars): ");
            username = scanner.nextLine().trim();

            if (!checkUserName(username)) {
                System.out.println("Invalid format. Username must contain underscore and be 5 characters or less.");
            } else if (registeredUsers.containsKey(username)) {
                System.out.println("Username taken. Please choose another.");
            } else {
                break;
            }
        }

        while (true) {
            System.out.print("Password (8+ characters, 1 uppercase, 1 number, 1 special): ");
            password = scanner.nextLine();
            if (checkPasswordComplexity(password)) break;
            System.out.println("Weak password. Must have 8+ chars, 1 uppercase, 1 number, 1 special character.");
        }

        while (true) {
            System.out.print("Phone (+27 and up to 10 digits): ");
            phone = scanner.nextLine().trim();
            if (checkCellPhoneNumber(phone)) break;
            System.out.println("Cell phone number is incorrectly formatted. Use format: +27xxxxxxxxx");
        }

        registeredUsers.put(username, password);
        userPhones.put(username, phone);
        System.out.println("User registered successfully!");
    }

    // Login
    private static void loginUser(Scanner scanner) {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (loginUser(username, password)) {
            currentUser = username;
            JOptionPane.showMessageDialog(null, "Welcome to QuickChat " + username + "!");
            openChatMenu();
        } else {
            System.out.println("Login failed. Please check your username and password.");
        }
    }

    private static boolean loginUser(String username, String password) {
        return registeredUsers.containsKey(username) && registeredUsers.get(username).equals(password);
    }

    private static void openChatMenu() {
        boolean keepGoing = true;
        while (keepGoing) {
            String choice = JOptionPane.showInputDialog(null,
                "Welcome to QuickChat Menu:\n" +
                "1. Write Draft\n" +
                "2. View Messages\n" +
                "3. View Drafts\n" +
                "4. Send Draft\n" +
                "5. Delete Draft\n" +
                "6. Exit",
                "QuickChat", JOptionPane.QUESTION_MESSAGE);

            if (choice == null) break;

            switch (choice) {
                case "1": writeDraft(); break;
                case "2": showMessages(); break;
                case "3": showDrafts(); break;
                case "4": sendDraft(); break;
                case "5": deleteDraft(); break;
                case "6":
                    JOptionPane.showMessageDialog(null, "Goodbye " + currentUser + "!");
                    keepGoing = false;
                    currentUser = null;
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Invalid option. Please choose 1-6.");
            }
        }
        saveMessagesToFile();
        saveStoredMessages();
    }

    // Messaging features
    private static void writeDraft() {
        String to = JOptionPane.showInputDialog("Send to (phone number or username):");
        if (to == null || to.trim().isEmpty()) return;
        
        String content = JOptionPane.showInputDialog("Message content:");
        if (content == null || content.trim().isEmpty()) return;

        drafts.add(new Message(to.trim(), content.trim(), LocalDateTime.now().toString(), currentUser, "Draft"));
        JOptionPane.showMessageDialog(null, "Message saved as draft successfully.");
    }

    private static void showDrafts() {
        if (drafts.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No drafts found.");
            return;
        }

        StringBuilder out = new StringBuilder("Your Drafts:\n\n");
        for (int i = 0; i < drafts.size(); i++) {
            Message d = drafts.get(i);
            out.append(i).append(") To: ").append(d.to)
               .append("\n   Message: ").append(d.content)
               .append("\n   Created: ").append(d.timestamp).append("\n\n");
        }

        JTextArea textArea = new JTextArea(out.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(400, 300));
        JOptionPane.showMessageDialog(null, scrollPane, "Your Drafts", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void sendDraft() {
        if (drafts.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No drafts to send.");
            return;
        }

        String input = JOptionPane.showInputDialog("Enter draft number to send (0-" + (drafts.size()-1) + "):");
        if (input == null) return;
        
        try {
            int idx = Integer.parseInt(input.trim());
            if (idx >= 0 && idx < drafts.size()) {
                Message sentMsg = drafts.remove(idx);
                sentMsg.flag = "Sent";
                sentMsg.timestamp = LocalDateTime.now().toString(); // Update timestamp when sent
                messages.add(sentMsg);
                sentMessages.add(sentMsg);
                messageHashes.add(sentMsg.messageHash);
                messageIDs.add(sentMsg.messageID);
                JOptionPane.showMessageDialog(null, "Draft sent successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Invalid draft number. Please enter a number between 0 and " + (drafts.size()-1));
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number.");
        }
    }

    private static void deleteDraft() {
        if (drafts.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No drafts to delete.");
            return;
        }

        String input = JOptionPane.showInputDialog("Enter draft number to delete (0-" + (drafts.size()-1) + "):");
        if (input == null) return;
        
        try {
            int idx = Integer.parseInt(input.trim());
            if (idx >= 0 && idx < drafts.size()) {
                drafts.remove(idx);
                JOptionPane.showMessageDialog(null, "Draft deleted successfully.");
            } else {
                JOptionPane.showMessageDialog(null, "Invalid draft number. Please enter a number between 0 and " + (drafts.size()-1));
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number.");
        }
    }

    private static void showMessages() {
        StringBuilder out = new StringBuilder("Your Messages:\n\n");
        boolean found = false;

        for (Message msg : messages) {
            if (msg.sender.equals(currentUser) || msg.to.equals(currentUser)) {
                out.append(msg.toString()).append("--------------------\n");
                found = true;
            }
        }

        if (!found) out.append("No messages found.");
        
        JTextArea textArea = new JTextArea(out.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(500, 400));
        JOptionPane.showMessageDialog(null, scrollPane, "Your Messages", JOptionPane.INFORMATION_MESSAGE);
    }

    // Validations
    public static boolean checkUserName(String username) {
        return username != null && !username.trim().isEmpty() && 
               username.length() <= 5 && username.contains("_");
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
        if (phone == null || phone.trim().isEmpty() || !phone.startsWith("+")) return false;

        String digits = phone.substring(1).replaceAll("\\D", "");
        // Check if it's a valid format: +27 followed by up to 10 digits
        return digits.startsWith("27") && digits.length() >= 10 && digits.length() <= 13;
    }

    // Persistence
    @SuppressWarnings("unchecked")
    private static void loadMessagesFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(MESSAGES_FILE))) {
            messages = (List<Message>) ois.readObject();
            System.out.println("Messages loaded: " + messages.size());
        } catch (FileNotFoundException e) {
            System.out.println("No saved messages found, starting fresh.");
        } catch (Exception e) {
            System.out.println("Error loading messages: " + e.getMessage());
        }
    }

    private static void saveMessagesToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MESSAGES_FILE))) {
            oos.writeObject(messages);
            System.out.println("Messages saved: " + messages.size());
        } catch (IOException e) {
            System.out.println("Error saving messages: " + e.getMessage());
        }
    }
}