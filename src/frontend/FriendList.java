package frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.List;
import java.util.StringTokenizer;

public class FriendList extends JFrame {
    private JList<String> friendList;
    private JPanel friendPanel;
    private JButton chatButton;
    private JScrollPane friendScroll;

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    public FriendList(PrintWriter printWriterOut) {
        chatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> selectedValuesList = friendList.getSelectedValuesList();
                StringTokenizer stringTokenizer;
                String members;
                if (selectedValuesList.size() > 1) {
                    String chatName = getChatName();
                    members = chatName.concat(" ");
                    printWriterOut.println("/newChat " + chatName);
                    for (String user : selectedValuesList) {
                        stringTokenizer = new StringTokenizer(user);
                        members += stringTokenizer.nextToken().concat(" ");
                    }
                    printWriterOut.println("/addChatMember " + members);
                    printWriterOut.println("/to room " + chatName);
                } else if (selectedValuesList.size() == 1) {
                    stringTokenizer = new StringTokenizer(selectedValuesList.get(0));
                    printWriterOut.println("/to user " + Integer.parseInt(stringTokenizer.nextToken()));
                } else {
                    JOptionPane.showMessageDialog(null, "No user seleced");
                }
            }
        });
    }

    public void init() {
        setTitle("Znajomi");
        setContentPane(friendPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        Dimension dim = getToolkit().getScreenSize();
        Rectangle aBounds = getBounds();
        setLocation((dim.width - aBounds.width) / 2, (dim.height - aBounds.height) / 2);
        setVisible(true);
    }

    public void addFriends(String users) {
        DefaultListModel listModel = new DefaultListModel();
        for (String user : users.split(";")) {
            listModel.addElement(user);
        }
        friendList.setModel(listModel);
    }

    private void createUIComponents() {
        friendPanel = new JPanel();
        chatButton = new JButton();
        friendList = new JList();
        friendScroll = new JScrollPane();
    }

    private String getChatName() {
        JPanel newChatPanel = new JPanel();
        JLabel chatLabel = new JLabel("Podaj nazwę chatu:");
        JTextField chatName = new JTextField();
        newChatPanel.setLayout(new BorderLayout());
        newChatPanel.add(chatLabel, BorderLayout.NORTH);
        newChatPanel.add(chatName, BorderLayout.SOUTH);
        JOptionPane.showConfirmDialog(null, newChatPanel, "Nowy chat",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return chatName.getText();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        friendPanel = new JPanel();
        friendPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(25, 25, 25, 25), -1, -1));
        friendPanel.setMaximumSize(new Dimension(2147483647, 2147483647));
        friendPanel.setMinimumSize(new Dimension(200, 400));
        friendPanel.setPreferredSize(new Dimension(300, 600));
        friendScroll = new JScrollPane();
        friendPanel.add(friendScroll, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        friendList = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        friendList.setModel(defaultListModel1);
        friendScroll.setViewportView(friendList);
        chatButton = new JButton();
        chatButton.setText("Chat");
        friendPanel.add(chatButton, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return friendPanel;
    }
}