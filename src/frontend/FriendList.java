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
    public JButton consoleButton;
    private int userID;
    private ChatView chatView;

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

    public FriendList(PrintWriter printWriterOut, int userId) {
        this.userID = userId;
        chatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> selectedValuesList = friendList.getSelectedValuesList();
                StringTokenizer stringTokenizer;
                String addNewMembers;
                if (selectedValuesList.size() > 1) {
                    String chatName = getChatName();
                    addNewMembers = chatName.concat("</> ");
                    printWriterOut.println("/newChat " + chatName);
                    for (String user : selectedValuesList) {
                        stringTokenizer = new StringTokenizer(user);
                        addNewMembers += stringTokenizer.nextToken().concat(" ");
                    }
                    printWriterOut.println("/addChatMember " + addNewMembers);
                    printWriterOut.println("/to room " + chatName);
                    chatView = new ChatView(printWriterOut, userID, chatName);
                    chatView.init(chatName);
                } else if (selectedValuesList.size() == 1) {
                    printWriterOut.println("/selectOption " + selectedValuesList.get(0));
                } else {
                    JOptionPane.showMessageDialog(null, "No user seleced");
                }
            }
        });
        consoleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printWriterOut.println("/showConsole");
            }
        });
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

    public void addFriends(String users) {
        DefaultListModel listModel = new DefaultListModel();
        for (String user : users.split(";")) {
            listModel.addElement(user);
        }

        friendList.setModel(listModel);
    }

    public ChatView getChatView() {
        return chatView;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
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
        friendPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(25, 25, 25, 25), -1, -1));
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
        consoleButton = new JButton();
        consoleButton.setText("Console");
        friendPanel.add(consoleButton, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return friendPanel;
    }
}
