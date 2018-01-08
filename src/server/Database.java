/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import common.User;

import java.util.Set;
import java.util.HashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author mariusz
 */
public class Database {

    private Connection dbConn;

    public Database(Connection dbConn) throws SQLException {
        this(dbConn, null);
    }

    public Database(Connection dbConn, String adminPassword) throws SQLException {
        this.dbConn = dbConn;
        if (adminPassword != null) {
            Statement st = dbConn.createStatement();
            try {
                st.execute("DROP TABLE \"user\"");
                st.execute("DROP TABLE friend");
            } catch (SQLException e) {
            }
            st.execute("CREATE TABLE \"user\" (" +
                    "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                    "firstName VARCHAR(64) NOT NULL," +
                    "lastName VARCHAR(64) NOT NULL," +
                    "passwordHash VARCHAR(64) NOT NULL," +
                    "CONSTRAINT primary_key PRIMARY KEY (id)" +
                    ")");
            st.execute("Create table friend( " +
                    "user_id INTEGER, related_user_id INTEGER, " +
                    "CONSTRAINT friend_pk primary key (user_id, related_user_id))");
            insertUser(new User("Admin", "Istrator", adminPassword, "MD5"));
//            Set<Integer> ids = getUserIds("%");
//            for(Integer id: ids) {
//                System.out.println("#" + id + ": " + getUser(id));
//            }
        }
    }

    public int insertUser(User u) throws SQLException {
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
        PreparedStatement st = dbConn.prepareStatement("SELECT id, firstName, lastName, passwordHash FROM \"user\" WHERE id=?");
        st.setInt(1, id);
        ResultSet rs = st.executeQuery();
        if (!rs.next()) return null;
        return new User(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
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

    public void insertFriend(String userNumber, String friendNumber) throws SQLException {
        PreparedStatement st = dbConn.prepareStatement("INSERT INTO friend VALUES (?, ?)");
        st.setInt(1, Integer.parseInt(userNumber));
        st.setInt(2, Integer.parseInt(friendNumber));
        st.executeUpdate();
    }

    public Set<String> myFriends(String userNumber) throws SQLException {
        Set<String> userNames = new HashSet<>();
        StringBuilder sb = new StringBuilder("SELECT f2.user_id FROM friend as f1 ");
        sb.append("join friend as f2 on f1.user_id = f2.related_user_id");
        sb.append("where f1.user_id = ?");

        PreparedStatement st = dbConn.prepareStatement(sb.toString());
        st.setString(1, userNumber);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            userNames.add(rs.getString(1));
        }
        return userNames;
    }
}