/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import common.User;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * @author Mariusz
 */
public class Server implements Runnable {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    private static int port;
    private static Set<Server> servers = new HashSet<>();
    private static JLabel nThreadsLabel;
    private static JLabel nRegisteredUsersLabel;
    private static Database db;

    public static void main(String[] args) throws IOException, SQLException {

        ServerSocket ssock = null;
        String dbURL = null;
        Connection dbConn = null;
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("Server.properties"));

            Class.forName(props.getProperty("dbDriver")).newInstance();
            dbURL = props.getProperty("dbURL");
            dbConn = DriverManager.getConnection(dbURL);
            boolean dbInit = Boolean.parseBoolean(props.getProperty("dbInit", "false"));
            String adminPassword = dbInit ? props.getProperty("adminPassword", "admin") : null;
            db = new Database(dbConn, adminPassword);

            port = Integer.parseInt(props.getProperty("port"));
            ssock = new ServerSocket(port);
        } catch (IOException e) {
            dbConn.close();
            JOptionPane.showMessageDialog(null, "While binding port " + port + "\n" + e);
            System.exit(1);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
            //JOptionPane.showMessageDialog(null, "Using DB " + dbURL + " not possible\n" + e);
            e.printStackTrace();
            System.exit(2);
        }

        JFrame mainWindow = new JFrame("Communicator server on port " + port);
        mainWindow.setSize(300, 120);
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel interior = new JPanel();
        interior.setBorder(new EmptyBorder(10, 10, 10, 10));
        interior.setLayout(new GridLayout(2, 2));
        interior.add(new JLabel("Active threads", JLabel.LEFT));
        nThreadsLabel = new JLabel("0", JLabel.RIGHT);
        interior.add(nThreadsLabel);
        interior.add(new JLabel("Registered users", JLabel.LEFT));
        nRegisteredUsersLabel = new JLabel("", JLabel.RIGHT);
        interior.add(nRegisteredUsersLabel);
        refreshView(true);
        Dimension dim = mainWindow.getToolkit().getScreenSize();
        Rectangle abounds = mainWindow.getBounds();
        mainWindow.setLocation((dim.width - abounds.width) / 2, (dim.height - abounds.height) / 2);
        mainWindow.add(interior);
        mainWindow.setVisible(true);

        for (; ; ) {
            Socket sock = ssock.accept();
            Server server = new Server(sock);
            new Thread(server).start();
        }
    }

    private Socket sock;
    private PrintWriter out;
    private User user;
    private String sendTo = null;
    private static final int NUMBER_OF_REQ_FILDS_REG = 3;
    private static final int NUMBER_OF_REQ_FILDS_LOG = 2;
    private static final String HASH_TYPE = "MD5";

    private Server(Socket sock) throws IOException {
        this.sock = sock;
    }

    @Override
    public void run() {
        servers.add(this);
        refreshView(false);
        try {
            out = new PrintWriter(sock.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
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
                if (s.charAt(0) == '/') {
                    // out.println("You entered a command " + s);
                    StringTokenizer st = new StringTokenizer(s);
                    String cmd = st.nextToken();
                    switch (cmd) {
                        case "/register":
                            if (st.countTokens() == NUMBER_OF_REQ_FILDS_REG) {
                                try {
                                    user = new User("", st.nextToken(), st.nextToken(), st.nextToken(), HASH_TYPE);
                                    user.setUserNumber(String.valueOf(db.insertUser(user)));
                                    out.println("Welcome on the board, your number: " + user.getUserNumber());
                                } catch (Exception e) {
                                    out.println("Something went wrong:\n" + e.getMessage());
                                }
                            } else {
                                out.println("Incorrect number of arguments");
                            }
                            break;
                        case "/login":
                            if (st.countTokens() == NUMBER_OF_REQ_FILDS_LOG) {
                                try {
                                    int number = Integer.parseInt(st.nextToken());
                                    String password = st.nextToken();

                                    user = db.getUser(number);
                                    if (user.getPasswordHash().equals(User.makeHash(password, HASH_TYPE))) {
                                        out.println("Welcome on the board " + user.getFirstName() + " " + user.getLastName());
                                    } else {
                                        user = null;
                                        out.println("Bad credentials, try again");
                                    }

                                } catch (Exception e) {
                                    out.println("Something went wrong:\n" + e.getMessage());
                                }

                            } else {
                                out.println("Incorrect number of arguments");
                            }
                            break;
                        case "/unregister":
                            if (user != null) {
                                try {
                                    out.println("You are unregistered");
                                    db.deleteUser(Integer.parseInt(user.getUserNumber()));
                                    user = null;
                                } catch (Exception e) {
                                    out.println("Something went wrong:\n" + e.getMessage());
                                }
                            } else {
                                out.println("You have to be logged in");
                            }
                            break;
                        case "/to":
                            if (st.hasMoreTokens()) {
                                sendTo = st.nextToken();
                                out.println("You have set default recipient to " + sendTo);
                            } else {
                                sendTo = null;
                                out.println("Default recipient unset");
                            }
                            break;
                        case "/who":
                            for (Server server : servers) {
                                out.print((server.user != null ? server.user.getUserNumber() : "[not logged in]") + " ");
                                if (server == this) out.print("(me)");
                                out.println(" from " + sock.getRemoteSocketAddress());
                            }
                            break;
                        case "/whoami":
                            if (user != null && user.getUserNumber() != null) {
                                out.println(user.getUserNumber() + " -> " + sendTo);
                            } else {
                                out.println("You are not logged in");
                            }
                            break;
                        case "/invite":
                            if (st.hasMoreTokens()) {
                                try {
                                    db.insertFriend(user.getUserNumber(), st.nextToken());
                                    out.println("Invitation sent");
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                out.println("Incorrect number of arguments");
                            }
                            break;
                        case "/friends":
                            try {
                                out.println("Your friends:");
                                Set<String> numbers = db.myFriends(user.getUserNumber());
                                for (String number : numbers) {
                                    out.println(number);
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "/exit":
                            break mainLoop;
                        default:
                            out.println("Unknown command " + cmd);
                    }
                } else {
                    if (user != null && user.getUserNumber() != null) {
                        if (sendTo != null) {
                            int count = 0;
                            for (Server server : servers) {
                                if (sendTo.equals(server.user.getUserNumber())) {
                                    server.out.println(s);
                                    count++;
                                }
                            }
                            out.println("Message has sent to " + count + " recipient(s)");
                        } else {
                            out.println("You should set default recipient");
                        }
                    } else {
                        out.println("You have to log in first");
                    }
                }
            }
        } catch (IOException e) {
        }
        servers.remove(this);
        try {
            sock.close();
        } catch (Exception e) {
        }
        refreshView(false);
    }

    private static void refreshView(boolean withDB) {
        nThreadsLabel.setText("" + servers.size());
        if (withDB) {
            try {
                nRegisteredUsersLabel.setText("" + db.countUsers());
            } catch (SQLException ex) {
                nRegisteredUsersLabel.setText("n/a");
            }
        }
    }
}
