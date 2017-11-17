/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * @author Mariusz
 */
public class Server implements Runnable {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    private static int port;
    private static JLabel nThreadsLabel;
    private static HashSet<Server> servers = new HashSet<>();
    private static Chat chat = new Chat();

    public static void main(String[] args) throws IOException {
        ServerSocket ssock = null;
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("Server.properties"));
            port = Integer.parseInt(props.getProperty("port"));
            ssock = new ServerSocket(port);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "While binding port " + port + "\n" + e);
            System.exit(1);
        }

        JFrame mainWindow = new JFrame("Communicator server on port " + port);
        mainWindow.setSize(400, 80);
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container interior = mainWindow.getContentPane();
        interior.setLayout(new GridLayout(1, 2));
        interior.add(new JLabel("Active threads", JLabel.CENTER));
        nThreadsLabel = new JLabel("0", JLabel.CENTER);
        interior.add(nThreadsLabel);
        Dimension dim = mainWindow.getToolkit().getScreenSize();
        Rectangle abounds = mainWindow.getBounds();
        mainWindow.setLocation((dim.width - abounds.width) / 2, (dim.height - abounds.height) / 2);
        mainWindow.setVisible(true);

        for (; ; ) {
            Socket sock = ssock.accept();
            Server server = new Server(sock);
            servers.add(server);
            new Thread(server).start();
        }
    }

    private Socket sock;
    private String login = null;
    private String sendTo = null;
    private String chatName = null;
    private String memeberName;
    private Server memeberServer = new Server();
    private PrintWriter out = null;

    private Server(Socket sock) throws IOException {
        this.sock = sock;
    }

    @Override
    public void run() {
        changeNThreads();
        try {
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
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
                        case "/login":
                            if (st.hasMoreElements()) {
                                login = st.nextToken();
                                out.println("Welcome on the board, " + login);
                            } else {
                                out.println("Login can not be empty!");
                            }
                            break;
                        case "/whoami":
                            if (login != null)
                                out.println(login);
                            else
                                out.println("You are not logged in");
                            break;
                        case "/who":
                            for (Server server : servers) {
                                out.println(server.login != null ? server.login + " /to " + server.sendTo : "Empty login");
                                server.out = new PrintWriter(server.sock.getOutputStream(), true);
                                server.out.println("/ready " + login);
                            }
                            break;
                        case "/chat":
                            if (st.hasMoreElements()) {
                                chatName = st.nextToken();
                                chat.init(chatName, this);
                            } else {
                                out.println("Chat name cannot be empty!");
                            }
                            break;
                        case "/join":
                            if (st.hasMoreElements()) {
                                chatName = st.nextToken();
                                memeberName = st.nextToken();

                                for (Server s : servers) {
                                    if (s.login.equals(memeberName)) {
                                        memeberServer = s;
                                    }
                                }
                                chat.addMembersToChat(chatName, memeberServer);
                            } else {
                                out.println("Chat name cannot be empty!");
                            }
                            break;
                        case "/to":
                            sendTo = st.nextToken();
                            for (Server server : servers) {
                                if (server.login.equals(sendTo)) {
                                    server.out = new PrintWriter(server.sock.getOutputStream(), true);
                                    server.out.println("/ready " + login);
                                }
                            }
                            break;
                        case "/exit":
                            servers.remove(this);
                            sock.close();
                            break mainLoop;
                        default:
                            out.println("Unknown command " + cmd);
                    }
                } else {
                    if (login != null) {
                        sendMessage(s);
                        out.println("You entered data [" + s + "]");
                    } else {
                        out.println("You have to log in first");
                    }
                }
            }
        } catch (IOException e) {
        }
        changeNThreads();
    }

    private void sendMessage(String message) throws IOException {
        for (Server server : servers) {
            if (sendTo != null && server.login.equals(sendTo)) {
                server.out = new PrintWriter(server.sock.getOutputStream(), true);
                synchronized (server) {
                    server.out.println("/from " + login);
                    server.out.println(message);
                }
            }
        }
    }

    private synchronized static void changeNThreads() {
        nThreadsLabel.setText("" + servers.size());
    }
}