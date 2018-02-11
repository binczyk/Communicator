/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import common.ChatRoom;
import common.Message;
import common.User;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * @author mariusz
 */
public final class Database {

    public static final String ERRMSG = "Error with server DB";

    private Connection dbConn;

    public Database() throws IOException, SQLException {
        connect();
    }

    public Database(Connection dbConn) throws SQLException {
        this(dbConn, null);
    }

    public Database(Connection dbConn, String adminPassword) throws SQLException {
        this.dbConn = dbConn;
        if (adminPassword != null) {
            Statement st = dbConn.createStatement();
            try {
                st.execute("DROP TABLE \"user\"");
            } catch (SQLException e) {
            }
            st.execute("CREATE TABLE \"user\" (" +
                    "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                    "firstName VARCHAR(64) NOT NULL," +
                    "lastName VARCHAR(64) NOT NULL," +
                    "passwordHash VARCHAR(64) NOT NULL," +
                    "is_active INTEGER CHECK (is_active in (1,0)) DEFAULT 0," +
                    "CONSTRAINT user_primary_key PRIMARY KEY (id)" +
                    ")");
            try {
                st.execute("DROP TABLE \"friend\"");
            } catch (SQLException e) {
            }
            st.execute("CREATE TABLE \"friend\" (" +
                    "id1 INTEGER NOT NULL," +
                    "id2 INTEGER NOT NULL" +
                    ")");
            addUser(new User("Admin", "Istrator", adminPassword, "MD5"));
//            Set<Integer> ids = getUserIds("%");
//            for(Integer id: ids) {
//                System.out.println("#" + id + ": " + getUser(id));
//            }
            try {
                st.execute("DROP TABLE \"message\"");
            } catch (SQLException e) {
            }
            st.execute("CREATE TABLE \"message\" (" +
                    "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                    "msgsent TIMESTAMP," +
                    "msgread TIMESTAMP," +
                    "msgfrom INTEGER NOT NULL," +
                    "msgto INTEGER NOT NULL," +
                    "content VARCHAR(1024)," +
                    "receiver VARCHAR(1024) CHECK (receiver in ('user','chat'))," +
                    "CONSTRAINT message_primary_key PRIMARY KEY (id)" +
                    ")");
            try {
                st.execute("DROP TABLE \"chat\"");
            } catch (SQLException e) {
            }
            st.execute("CREATE TABLE \"chat\" (" +
                    "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                    "name VARCHAR(1024)," +
                    "CONSTRAINT chat_pk PRIMARY KEY (id)" +
                    ")");
            try {
                st.execute("DROP TABLE \"members\"");
            } catch (SQLException e) {
            }
            st.execute("CREATE TABLE \"members\" (" +
                    "chat_id INTEGER NOT NULL," +
                    "user_id INTEGER NOT NULL, " +
                    "is_admin INTEGER, " +
                    "CONSTRAINT chat_user_uq UNIQUE (chat_id, user_id)" +
                    ")");
        }
    }

    public int addUser(User u) throws SQLException {
        PreparedStatement st = dbConn.prepareStatement("INSERT INTO \"user\" (firstName, lastName, passwordHash) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        st.setString(1, u.getFirstName());
        st.setString(2, u.getLastName());
        st.setString(3, u.getPasswordHash());
        st.executeUpdate();
        ResultSet rs = st.getGeneratedKeys();
        rs.next();
        return rs.getInt(1);
    }

    public User getUser(int id) throws SQLException {
        if (id == 0) return null;
        PreparedStatement st = dbConn.prepareStatement("SELECT firstName, lastName, passwordHash FROM \"user\" WHERE id=?");
        st.setInt(1, id);
        ResultSet rs = st.executeQuery();
        rs.next();
        return new User(rs.getString(1), rs.getString(2), rs.getString(3));
    }

    public Set<Integer> getUserIds(String pattern) throws SQLException {
        Set<Integer> userIds = new HashSet<>();
        PreparedStatement st = dbConn.prepareStatement("SELECT id FROM \"user\" WHERE firstName LIKE ? OR lastName LIKE ?");
        st.setString(1, pattern);
        st.setString(2, pattern);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            userIds.add(rs.getInt(1));
        }
        return userIds;
    }

    public void updateUser(int id, User u) throws SQLException {
        PreparedStatement st = dbConn.prepareStatement("UPDATE \"user\" SET firstName=?, lastName=?, passwordHash=? WHERE id=?");
        st.setString(1, u.getFirstName());
        st.setString(2, u.getLastName());
        st.setString(3, u.getPasswordHash());
        st.setInt(4, id);
        st.executeUpdate();
    }

    public void deleteUser(int id) throws SQLException {
        PreparedStatement st = dbConn.prepareStatement("DELETE FROM \"user\" WHERE id=?");
        st.setInt(1, id);
        st.executeUpdate();
    }

    public int countUsers() throws SQLException {
        Statement st = dbConn.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM \"user\"");
        rs.next();
        return rs.getInt(1);
    }

    public void addFriendship(int id1, int id2) throws SQLException, IllegalArgumentException {
        if (getUser(id1) == null || getUser(id2) == null) {
            throw new IllegalArgumentException();
        }
        PreparedStatement st = dbConn.prepareStatement("INSERT INTO \"friend\" (id1, id2) VALUES (?, ?)");
        st.setInt(1, id1);
        st.setInt(2, id2);
        st.executeUpdate();
    }

    public void deleteFriendship(int id1, int id2) throws SQLException {
        PreparedStatement st = dbConn.prepareStatement("DELETE FROM \"friend\" WHERE id1=? AND id2=?");
        st.setInt(1, id1);
        st.setInt(2, id2);
        st.executeUpdate();
    }

    public boolean isFriend(int id1, int id2) throws SQLException {
        PreparedStatement st = dbConn.prepareStatement("SELECT COUNT(*) FROM \"friend\" WHERE id1=? AND id2=?");
        st.setInt(1, id1);
        st.setInt(2, id2);
        ResultSet rs = st.executeQuery();
        rs.next();
        return rs.getInt(1) > 0;
    }

    public List<User> getFriends(int userId) throws SQLException {
        List<User> friends = new ArrayList<>();
        PreparedStatement st = dbConn.prepareStatement("SELECT id, FIRSTNAME, LASTNAME, is_active\n" +
                "FROM \"user\" WHERE id IN (SELECT ID1 FROM \"friend\" WHERE ID2 = ?) order by ID asc");
        st.setInt(1, userId);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            friends.add(new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4) == 0 ? false : true));
        }
        return friends;
    }

    public int saveMessage(Message msg) throws SQLException {
        PreparedStatement st = dbConn.prepareStatement("INSERT INTO \"message\" (msgsent, msgread, msgfrom, msgto, content, receiver) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        st.setTimestamp(1, msg.getSent());
        st.setTimestamp(2, msg.getRead());
        st.setInt(3, msg.getFrom());
        st.setInt(4, msg.getTo());
        st.setString(5, msg.getContent());
        st.setString(6, msg.getType());
        st.executeUpdate();
        ResultSet rs = st.getGeneratedKeys();
        rs.next();
        return rs.getInt(1);
    }

    public Message restoreMessage(int id, boolean delete) throws SQLException {
        if (id == 0) return null;
        PreparedStatement st = dbConn.prepareStatement("SELECT msgsent, msgread, msgfrom, msgto, content FROM \"message\" WHERE id=?");
        st.setInt(1, id);
        ResultSet rs = st.executeQuery();
        rs.next();
        Message msg = new Message(rs.getTimestamp(1), rs.getTimestamp(2), rs.getInt(3), rs.getInt(4), rs.getString(5));
        if (delete) {
            st = dbConn.prepareStatement("DELETE FROM \"message\" WHERE id=?");
            st.setInt(1, id);
            st.executeUpdate();
        }
        return msg;
    }

    public void markMessageAsRead(int id) throws SQLException {
        PreparedStatement st = dbConn.prepareStatement("UPDATE \"message\" SET msgread=? WHERE id=?");
        st.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        st.setInt(2, id);
        st.executeUpdate();
    }

    public void setActive(int id, boolean isActive) throws SQLException {
        PreparedStatement st = dbConn.prepareStatement("UPDATE \"user\" SET is_active=? WHERE id=?");
        st.setInt(1, isActive ? 1 : 0);
        st.setInt(2, id);
        st.executeUpdate();
    }

    public List<Message> getNotReadMessages(int id, String receiver) throws SQLException {
        List<Message> notRead = new ArrayList<>();
        PreparedStatement st = dbConn.prepareStatement("SELECT * FROM \"message\" WHERE MSGTO=? and MSGREAD is null and receiver = ? order by MSGSENT");
        st.setInt(1, id);
        st.setString(2, receiver);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            notRead.add(new Message(rs.getInt(1), rs.getTimestamp(2), rs.getTimestamp(3),
                    rs.getInt(4), rs.getInt(5), rs.getString(6)));
        }
        return notRead;
    }

    public int addChat(String chatName, int owner_id) throws SQLException {
        PreparedStatement st = dbConn.prepareStatement("INSERT INTO \"chat\" (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        st.setString(1, chatName.trim());
        st.executeUpdate();
        ResultSet rs = st.getGeneratedKeys();
        rs.next();
        addMember(chatName, owner_id, true);
        return rs.getInt(1);
    }

    public ChatRoom findChatByName(String name) throws SQLException {
        PreparedStatement st = dbConn.prepareStatement("SELECT id, name, user_id " +
                "FROM \"chat\" left join \"members\" on chat_id = id and is_admin = 1 WHERE NAME = ?");
        st.setString(1, name);
        ResultSet rs = st.executeQuery();
        rs.next();

        return new ChatRoom(rs.getInt(1), rs.getString(2), rs.getInt(3));
    }

    public void addMember(String chatName, Integer usersId, boolean is_admin) throws SQLException {
        ChatRoom chatRoom = findChatByName(chatName);
        PreparedStatement st = dbConn.prepareStatement("INSERT INTO \"members\" (chat_id, user_id, is_admin) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
        st.setInt(1, chatRoom.getId());
        st.setInt(2, usersId);
        st.setInt(3, is_admin ? 1 : 0);
        st.executeUpdate();
        ResultSet rs = st.getGeneratedKeys();
        rs.next();
    }


    private Database connect() throws IOException, SQLException {
        String dbURL = null;
        Connection dbConn = null;
        String adminPassword = "";
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("Server.properties"));

            Class.forName(props.getProperty("dbDriver")).newInstance();
            dbURL = props.getProperty("dbURL");
            dbConn = DriverManager.getConnection(dbURL);
            boolean dbInit = Boolean.parseBoolean(props.getProperty("dbInit", "false"));
            adminPassword = dbInit ? props.getProperty("adminPassword", "admin") : null;

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
            e.printStackTrace();
            System.exit(2);
        }
        return new Database(dbConn, adminPassword);
    }

    private LinkedList<Integer> findMembersByChatName(String name) throws SQLException {
        LinkedList<Integer> userIds = new LinkedList<>();

        PreparedStatement st = dbConn.prepareStatement("SELECT user_id FROM \"chat\" " +
                "left join \"members\" on chat_id = id WHERE name = ? order by name asc");
        st.setString(1, name);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            userIds.add(rs.getInt(1));
        }

        return userIds;
    }

    public List<ChatRoom> findChatByUserId(int login) throws SQLException {
        List<ChatRoom> chatRooms = new ArrayList<>();

        PreparedStatement st = dbConn.prepareStatement("SELECT * FROM \"chat\" " +
                "left join \"members\" on chat_id = id WHERE user_id = ? order by name ASC");
        st.setInt(1, login);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            chatRooms.add(new ChatRoom(rs.getInt(1), rs.getString(2), rs.getInt(3)));
        }

        for (ChatRoom room : chatRooms) {
            room.setMembers(findMembersByChatName(room.getRoomName()));
        }

        return chatRooms;
    }
}