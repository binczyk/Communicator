/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import client.Client;
import common.ChatRoom;
import common.Message;
import common.User;
import frontend.ServerVisualization;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.FileSystemException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author Mariusz
 */
public class Server implements Runnable {

    public static final int UPLOADLIMIT = 1024 * 1024 * 1024;
    public static final int UPLOADBUFFERSIZE = 64 * 1024;

    private static int port;
    private static Set<Server> servers = new HashSet<>();
    private Map<String, ChatRoom> chatRooms = new HashMap<>();
    private static Database db;
    private Socket sock;
    private PrintWriter out;
    private int login = 0;
    private int sendTo = 0;
    private String sendToRoom = new String();
    private static ServerVisualization serverVisualization = new ServerVisualization();

    private Server(Socket sock) throws IOException {
        this.sock = sock;
    }

    public static void main(String[] args) throws IOException, SQLException {

        ServerSocket ssock = null;
        String dbURL = null;
        Connection dbConn = null;
        boolean startClientOnBoundPort = false;
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("Server.properties"));

            startClientOnBoundPort = Boolean.parseBoolean(props.getProperty("startClientOnBoundPort", "false"));
            port = Integer.parseInt(props.getProperty("port"));
            ssock = new ServerSocket(port);

            Class.forName(props.getProperty("dbDriver")).newInstance();
            dbURL = props.getProperty("dbURL");
            dbConn = DriverManager.getConnection(dbURL);
            boolean dbInit = Boolean.parseBoolean(props.getProperty("dbInit", "false"));
            String adminPassword = dbInit ? props.getProperty("adminPassword", "admin") : null;
            db = new Database(dbConn, adminPassword);

        } catch (IOException e) {
            if (startClientOnBoundPort) {
                Client.main(args);
            } else {
                JOptionPane.showMessageDialog(null, "While binding port " + port + "\n" + e);
                System.exit(1);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
            //JOptionPane.showMessageDialog(null, "Using DB " + dbURL + " not possible\n" + e);
            e.printStackTrace();
            System.exit(2);
        }

        if (ssock == null) return;

        serverVisualization.init(port);
        refreshView(serverVisualization, true);

        for (; ; ) {
            Socket sock = ssock.accept();
            Server server = new Server(sock);
            new Thread(server).start();
        }
    }


    private static void refreshView(ServerVisualization sv, boolean withDB) {
        if (servers != null) {
            sv.refrashLabel(String.valueOf(servers.size()));
        }
        if (withDB && db != null) {
            try {
                sv.refreshUserCount(String.valueOf(db.countUsers()));
            } catch (SQLException ex) {
                System.out.println(ex);
                sv.refreshUserCount("n/a");
            }
        }
    }

    @Override
    public void run() {
        User user = null;
        servers.add(this);
        refreshView(serverVisualization, false);
        try {
            out = new PrintWriter(sock.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out.println("Use /help for help");
            mainLoop:
            for (; ; ) {
                String s = null;
                try {
                    s = in.readLine();
                } catch (SocketException e) {
                    break;
                }
                if (s == null) break;
            /*
                interpretation of a command/data sent from clients
            */
                if (!s.trim().isEmpty() && s.charAt(0) == '/') {
                    StringTokenizer st = new StringTokenizer(s);
                    String cmd = st.nextToken();
                    switch (cmd) {
                        case "/login":
                            if (st.hasMoreTokens()) {
                                try {
                                    int loginCandidate = Integer.parseInt(st.nextToken());
                                    user = db.getUser(loginCandidate);
                                    String passwordHash = User.makeHash(st.hasMoreTokens() ? st.nextToken() : "", "MD5");
                                    if (!user.getPasswordHash().equals(passwordHash)) {
                                        out.println("/err Login failed");
                                    } else {
                                        login = loginCandidate;
                                        out.println("/succesful");
                                        out.println("Welcome on the board, " + user);
                                        db.setActive(login, true);
                                        updateFriendList();
                                        fetchMessages();
                                        notifyFriends();
                                    }
                                } catch (NumberFormatException ex) {
                                    out.println("/err Non-integer user id used");
                                } catch (SQLException ex) {
                                    out.println("/err No such user");
                                }
                            } else {
                                login = 0;
                                sendTo = 0;
                                out.println("You are logged out");
                            }
                            break;
                        case "/to":
                            if (st.hasMoreTokens()) {
                                String type = st.nextToken();
                                if (type.equalsIgnoreCase("user") && st.hasMoreTokens()) {
                                    try {
                                        int sendToCandidate = Integer.parseInt(st.nextToken());
                                        user = db.getUser(sendToCandidate);
                                        sendTo = sendToCandidate;
                                        sendToRoom = "";
                                        out.println("You have set default recipient to " + user);
                                    } catch (NumberFormatException ex) {
                                        out.println("/err Non-integer user id used");
                                    } catch (SQLException ex) {
                                        out.println("/err No such user");
                                    }
                                } else if (type.equalsIgnoreCase("room") && st.hasMoreTokens()) {
                                    String name = getRoomName(st);
                                    if (chatRooms.containsKey(name)) {
                                        sendToRoom = name;
                                        sendTo = 0;
                                    } else {
                                        out.println("/err No such room " + name);
                                    }
                                } else {
                                    out.println("/err No such type or no name given");
                                }

                            } else {
                                sendTo = 0;
                                out.println("Default recipient unset");
                            }
                            break;
                        case "/who":
                            for (Server server : servers) {
                                try {
                                    out.print((server.login > 0 ? db.getUser(server.login) : "[not logged in]") + " ");
                                } catch (SQLException ex) {
                                    out.print(s);
                                }
                                if (server == this) out.print("(me)");
                                out.println(" from " + sock.getRemoteSocketAddress());
                            }
                            break;
                        case "/whoami":
                            if (login > 0) {
                                try {
                                    out.println(db.getUser(login) + "\nWriting to " + db.getUser(sendTo));
                                } catch (SQLException ex) {
                                    out.println("/err " + Database.ERRMSG);
                                }
                            } else {
                                out.println("/err You are not logged in");
                            }
                            break;
                        case "/list":
                            String pattern = "%";
                            if (st.hasMoreTokens()) {
                                pattern = st.nextToken();
                            }

                            try {
                                Set<Integer> ids = db.getUserIds(pattern);
                                for (Integer id : ids) {
                                    user = db.getUser(id);
                                    out.println(id + ": " + user);
                                }
                            } catch (SQLException ex) {
                                out.println("/err " + Database.ERRMSG);
                            }

                            break;
                        case "/register":
                            try {
                                int id = db.addUser(new User(st.nextToken(), st.nextToken(), st.nextToken(), "MD5"));
                                out.println("Successfully registered as " + id);
                            } catch (NoSuchElementException ex) {
                                out.println("/err Use /register firstName lastName password");
                            } catch (SQLException ex) {
                                out.println("/err " + Database.ERRMSG);
                            }
                            break;
                        case "/unregister":
                            if (login > 0) {
                                try {
                                    db.deleteUser(login);
                                    login = 0;
                                    sendTo = 0;
                                } catch (Exception ex) {
                                    out.println("/err" + Database.ERRMSG);
                                }
                            } else {
                                out.println("/err You should log in first");
                            }
                            break;
                        case "/upload":
                            synchronized (sock) {
                                try {
                                    int bytesToRead = Integer.parseInt(st.nextToken());
                                    if (bytesToRead < 0 || bytesToRead > UPLOADLIMIT)
                                        throw new FileSystemException("File to upload too big");
                                    UUID uuid = UUID.randomUUID();
                                    out.println("/uploadready " + uuid);
                                    File uploadedFileName = new File("files/" + uuid);
                                    FileOutputStream fos = new FileOutputStream(uploadedFileName);
                                    byte[] buffer = new byte[UPLOADBUFFERSIZE];
                                    int n, bytesRead = 0;
                                    BufferedInputStream bis = new BufferedInputStream(sock.getInputStream());
                                    while (bytesRead < bytesToRead) {
                                        n = bytesToRead - bytesRead;
                                        if (n > buffer.length) n = buffer.length;
                                        n = bis.read(buffer, 0, n);
                                        if (n > 0) {
                                            fos.write(buffer, 0, n);
                                            fos.flush();
                                            bytesRead += n;
                                            out.println("/uploaded " + bytesRead + " " + bytesToRead);
                                        }
                                    }
                                    fos.close();
                                    out.println("/uploadcomplete " + uuid + " " + bytesToRead);
                                } catch (FileSystemException ex) {
                                    out.println("/err " + ex.getMessage());
                                } catch (NumberFormatException ex) {
                                    out.println("/err No file size");
                                }
                            }
                            break;
                        case "/download":
                            synchronized (sock) {
                                if (!st.hasMoreTokens()) {
                                    out.println("/err No file uuid");
                                } else {
                                    String fileName = st.nextToken();
                                    try {
                                        File file = new File("files/" + fileName);
                                        long fileSize = file.length();
                                        out.println("/downloadready " + fileSize);
                                        byte[] buffer = new byte[UPLOADBUFFERSIZE];
                                        try (FileInputStream fis = new FileInputStream(file)) {
                                            BufferedOutputStream bos = new BufferedOutputStream(sock.getOutputStream());
                                            long bytesToSend = fileSize;
                                            while (bytesToSend > 0) {
                                                long k = fis.read(buffer);
                                                if (k > bytesToSend) k = bytesToSend;
                                                bos.write(buffer, 0, (int) k);
                                                bos.flush();
                                                bytesToSend -= k;
                                            }
                                            fis.close();
                                        }
                                    } catch (FileNotFoundException ex) {
                                        out.println("/err No file " + fileName + " to download");
                                    } catch (IOException ex) {
                                        out.println("/err Error during download " + fileName + "(" + ex + ")");
                                    }
                                }
                            }
                            break;
                        case "/newChat":
                            if (st.hasMoreElements()) {
                                String roomName = getRoomName(st);
                                if (!chatRooms.containsKey(roomName)) {
                                    chatRooms.put(roomName, new ChatRoom(roomName, login));
                                    db.addChat(roomName, login);
                                    out.println("New room created " + roomName);
                                    notifyFriends();
                                } else {
                                    out.println("Room " + roomName + " already exists");
                                }

                            }
                            break;
                        case "/addChatMember":
                            if (st.hasMoreElements()) {
                                String roomName = st.nextToken();
                                if (chatRooms.containsKey(roomName) && chatRooms.get(roomName).getOwner() == login) {
                                    while (st.hasMoreElements()) {
                                        int member = Integer.parseInt(st.nextToken());
                                        chatRooms.get(roomName).addMember(member);
                                        db.addMember(roomName, member, false);
                                        out.println("New member added " + member);
                                    }
                                    updateRooms(roomName);
                                } else {
                                    out.println("Room " + roomName + " doesn't exist or you aren't a administrator");
                                }
                            }
                            break;
                        case "/signOutFromRoom":
                            if (st.hasMoreElements()) {
                                String roomName = st.nextToken();
                                chatRooms.get(roomName).singOut(login);
                                updateRooms(roomName);
                            }
                            break;
                        case "/closeRoom":
                            if (st.hasMoreElements()) {
                                String roomName = st.nextToken();
                                if (chatRooms.containsKey(roomName) && chatRooms.get(roomName).getOwner() == login) {
                                    chatRooms.remove(roomName);
                                    updateRooms(roomName);
                                    out.println("Room " + roomName + " closed");
                                } else {
                                    out.println("Room " + roomName + " doesn't exist or you aren't a administrator");
                                }
                            }
                            break;
                        case "/showRooms":
                            for (Map.Entry<String, ChatRoom> chatRoom : chatRooms.entrySet()) {
                                out.println(chatRoom.getValue().getRoomName());
                            }
                            break;
                        case "/showMembers":
                            if (st.hasMoreTokens()) {
                                String roomName = st.nextToken();
                                for (int member : chatRooms.get(roomName).getMembers()) {
                                    try {
                                        out.println((member > 0 ? db.getUser(member) : "[not logged in]"));
                                    } catch (SQLException e) {
                                        out.print(s);
                                    }
                                }
                            }
                            break;
                        case "/addFriend":
                            if (st.hasMoreTokens()) {
                                String nextToken = st.nextToken();
                                int friendId = Integer.parseInt(nextToken);
                                if (user != null) {
                                    try {
                                        db.addFriendship(login, friendId);
                                        sendMessageToUser("/from", friendId, user.getFirstName().concat(" ").concat(user.getLastName())
                                                .concat(" want to add you to friends list. Do you agree(yes, no)?"), "user");
                                    } catch (SQLException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                            break;
                        case "/from":
                            if (st.hasMoreTokens()) {
                                StringBuilder stringBuilder = new StringBuilder("/from ");
                                String userId = st.nextToken();
                                while (st.hasMoreTokens()) {
                                    stringBuilder.append(st.nextToken());
                                    stringBuilder.append(" ");
                                }
                                out.println(stringBuilder.toString().trim());
                            }
                            break;
                        case "/help":
                            BufferedReader help = new BufferedReader(new FileReader("help.txt"));
                            String line;
                            while ((line = help.readLine()) != null) {
                                out.println(line);
                            }
                            break;
                        case "/exit":
                            db.setActive(login, false);
                            break mainLoop;
                        default:
                            out.println("/err Unknown command " + cmd);
                    }
                } else {
                    if (login > 0) {
                        if (sendTo > 0 && !s.trim().isEmpty()) {
                            sendMessageToUser("/from", sendTo, s, "user");
                        } else if (!sendToRoom.isEmpty()) {
                            for (int memberId : chatRooms.get(sendToRoom).getMembers()) {
                                sendMessageToUser("/from ".concat(sendToRoom).concat(" "), memberId, s, "chat");
                            }
                        } else {
                            out.println("You should set default recipient");
                        }
                        db.setActive(login, false);
                    } else {
                        out.println("You have to log in first");
                    }
                }
            }
        } catch (
                IOException e)

        {
        } catch (SQLException e) {
            e.printStackTrace();
        }
        servers.remove(this);
        try {
            db.setActive(login, false);
            notifyFriends();
            sock.close();
        } catch (
                Exception e)

        {
        }

        refreshView(serverVisualization, false);

    }

    private String getRoomName(StringTokenizer st) {
        StringBuilder roomName = new StringBuilder();
        while (st.hasMoreTokens()) {
            roomName.append(st.nextToken());
            roomName.append(" ");
        }
        return roomName.toString();
    }

    private void notifyFriends() throws SQLException {
        for (Server server : servers) {
            server.updateFriendList();
        }
    }

    private String fetchFriends() throws SQLException {
        StringBuilder sb = new StringBuilder();
        for (User user : db.getFriends(login)) {
            sb.append(String.valueOf(user.getId()));
            sb.append(" ");
            sb.append(user.getFirstName());
            sb.append(" ");
            sb.append(user.getLastName());
            sb.append(" ");
            sb.append(user.isActive() ? "Active;" : "Inactive;");
        }
        return sb.toString().trim();
    }

    private String fetchChats() {
        StringBuilder sb = new StringBuilder();
        try {
            for (ChatRoom chatRoom : db.findChatByUserId(login)) {
                chatRooms.put(chatRoom.getRoomName(), chatRoom);
                sb.append(chatRoom.getRoomName().concat("(chat);"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString().trim();
    }

    private void updateFriendList() throws SQLException {
        StringBuilder builder = new StringBuilder("/friends ");
        builder.append(fetchChats());
        builder.append(fetchFriends());
        out.println(builder.toString());

    }

    private List<Message> fetchMessages() throws SQLException {
        List<Message> newMessages = db.getNotReadMessages(login, "user");
        newMessages.addAll(db.getNotReadMessages(login, "chat"));
        for (Message msg : newMessages) {
            out.println(msg.getContent());
            db.markMessageAsRead(msg.getId());
        }
        return newMessages;
    }

    private void sendMessageToUser(String type, int sendToId, String message, String reciverType) {
        try {
            Message msg = new Message(new Timestamp(System.currentTimeMillis()), null, login, sendToId, message, reciverType);
            int msgId = db.saveMessage(msg);
            int count = 0;
            for (Server server : servers) {
                if (sendToId == server.login) {
                    synchronized (sock) {
                        server.out.println(type.concat(" ").concat(String.valueOf(login)).concat("\n").concat(message));
                    }
                    count++;
                    if (count == 1) {
                        db.markMessageAsRead(msgId);
                    }
                }
            }
            out.println("Message has sent to " + count + " recipient(s)");
        } catch (SQLException ex) {
            out.println("/err" + Database.ERRMSG);
        }
    }

    private void updateRooms(String roomName) {
        if (chatRooms.containsKey(roomName)) {
            for (int member : chatRooms.get(roomName).getMembers()) {
                for (Server server : servers) {
                    if (server.login == member) {
                        server.chatRooms.put(roomName, chatRooms.get(roomName));
                    }
                }

            }
        } else {
            for (Server server : servers) {
                if (server.chatRooms.containsKey(roomName)) {
                    server.chatRooms.remove(roomName);
                }
            }
        }
    }

}
