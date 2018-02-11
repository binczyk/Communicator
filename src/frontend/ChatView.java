package frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;

public class ChatView extends JFrame implements ActionListener, KeyListener, WindowListener {
    private JPanel chatPanel;
    private JButton sendbutton;
    private JTextField inputChatField;
    private JTextArea chatText;
    private JScrollPane textScroll;
    private PrintWriter printWriterOut;
    private int userId;

    public ChatView(PrintWriter printWriterOut, int userId) {
        this.printWriterOut = printWriterOut;
        this.userId = userId;
        sendbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!inputChatField.getText().trim().isEmpty()) {
                    String msg = inputChatField.getText().concat("\n");
                    chatText.append("← ".concat(String.valueOf(userId).concat(": ")).concat(msg));
                    printWriterOut.println("/from ".concat(String.valueOf(userId)).concat(" ").concat(msg));
                    inputChatField.setText("");
                }
            }
        });
        inputChatField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        sendbutton.doClick();
                        break;
                }
            }
        });
    }

    public void init(String title) {
        setTitle("Chat: " + title);
        setContentPane(chatPanel);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        pack();
        Dimension dim = getToolkit().getScreenSize();
        Rectangle aBounds = getBounds();
        setLocation((dim.width - aBounds.width) / 2, (dim.height - aBounds.height) / 2);
        setVisible(true);
    }

    public void addMessage(String msg) {
        this.chatText.append("→ ".concat(msg).concat("\n"));
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    public PrintWriter getPrintWriterOut() {
        return printWriterOut;
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
        chatPanel = new JPanel();
        chatPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 3, new Insets(5, 5, 5, 5), -1, -1));
        chatPanel.setMinimumSize(new Dimension(500, 400));
        chatPanel.setPreferredSize(new Dimension(500, 400));
        inputChatField = new JTextField();
        chatPanel.add(inputChatField, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        textScroll = new JScrollPane();
        chatPanel.add(textScroll, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        chatText = new JTextArea();
        chatText.setEditable(false);
        textScroll.setViewportView(chatText);
        sendbutton = new JButton();
        sendbutton.setText("send");
        chatPanel.add(sendbutton, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return chatPanel;
    }
}