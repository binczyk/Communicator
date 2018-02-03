package frontend;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ServerVisualization {

    private JLabel nThreadsLabel;
    private JLabel nRegisteredUsersLabel;

    public void refrashLabel(String newLabel) {
        nThreadsLabel.setText("" + newLabel);
    }

    public void refreshUserCount(String userCount) {
        nRegisteredUsersLabel.setText("" + userCount);
    }

    public void init(int port) {
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
        Dimension dim = mainWindow.getToolkit().getScreenSize();
        Rectangle abounds = mainWindow.getBounds();
        mainWindow.setLocation((dim.width - abounds.width) / 2, (dim.height - abounds.height) / 2);
        mainWindow.add(interior);
        mainWindow.setVisible(true);
    }
}
