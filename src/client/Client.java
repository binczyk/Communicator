/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import javax.swing.JFrame;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Mariusz
 */
public class Client extends JFrame implements ActionListener, KeyListener, WindowListener, Runnable {

	private InetAddress addr;
	private int port;
        private String connectTo = null;

	private final JTextField input;
        private final ArrayList<String> history = new ArrayList<> ();
        private int historyPos = 0;
	private final JScrollPane scroller;
	private final JTextArea topPanel;
	private final JButton buttonOk;
	private PrintWriter out = null;
	private BufferedReader in = null;

	public Client(String title){
	 	super(title);
		setSize(500,400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container interior = getContentPane();
		interior.setLayout(new BorderLayout());
		topPanel = new JTextArea();
		topPanel.setEditable(false);
		scroller = new JScrollPane(topPanel);
		interior.add(scroller, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		input = new JTextField();
		bottomPanel.add(input, BorderLayout.CENTER);
		buttonOk = new JButton("OK");
		buttonOk.addActionListener(this);
		input.addKeyListener(this);
		bottomPanel.add(buttonOk, BorderLayout.EAST);
		interior.add(bottomPanel, BorderLayout.SOUTH);
		addWindowListener(this);
		Dimension dim = getToolkit().getScreenSize();
		Rectangle aBounds = getBounds();
		setLocation((dim.width - aBounds.width) / 2, (dim.height - aBounds.height) / 2);
	}

        @Override
	public void keyReleased(KeyEvent e) {}
	
        @Override
	public void keyPressed(KeyEvent e) {
            switch(e.getKeyCode()) {
                case KeyEvent.VK_UP:                    
                    if(historyPos > 0) {
                        historyPos--;
                        input.setText(history.get(historyPos));
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if(historyPos < history.size() - 1) {
                        historyPos++;
                        input.setText(history.get(historyPos));
                    } else {
                        historyPos = history.size();
                        input.setText("");
                    }    
                    break;
                case KeyEvent.VK_ENTER: buttonOk.doClick(); break;
            }
	}

        @Override
	public void keyTyped(KeyEvent e) {}
	
        @Override
	public void windowOpened(WindowEvent e) {
		input.requestFocus();
	}
        @Override
	public void windowClosed(WindowEvent e) {}
        @Override
	public void windowClosing(WindowEvent e) {}
        @Override
	public void windowActivated(WindowEvent e) {}
        @Override
	public void windowDeactivated(WindowEvent e) {}
        @Override
	public void windowIconified(WindowEvent e) {}
        @Override
	public void windowDeiconified(WindowEvent e) {}

        @Override
	public void actionPerformed(ActionEvent ae) {
		String s = input.getText();
		if(s.equals("")) return;
		try {
			out.println(s);
                        history.add(s);
                        historyPos = history.size();
		} catch(Exception e) { JOptionPane.showMessageDialog(null, e); System.exit(0); }
		input.setText(null);	
	}
		
    @Override
	public void run() {
		for(;;) {
			try {
                            if(in == null) connect();
			    String s = in.readLine();
			    if(s == null) {
					JOptionPane.showMessageDialog(null, "Connection closed by the server");
					System.exit(0);
			    } 
			    topPanel.append(s + "\n");
                            scroller.getVerticalScrollBar().setValue(scroller.getVerticalScrollBar().getMaximum());
			} catch(Exception e) {
                            if(JOptionPane.showConfirmDialog(null, e + "\n\n" + "Reconnect?", "Reconnect", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                                in = null;
                                continue;
                            } else {
                                System.exit(0);
                            }
                        }
		}
	}
	
        private void connect() throws IOException {
	    setTitle("Connecting to " + connectTo);
            Socket sock =  new Socket(addr.getHostName(), port);
	    out = new PrintWriter(sock.getOutputStream(), true);
	    in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
	    setTitle("Connected to " + connectTo);
        }
        
	public static void main (String[] args) {

            Client f = new Client("Communicator client");
		
		try {
			Properties props = new Properties();
			props.load(new FileInputStream("Client.properties"));
			f.addr = InetAddress.getByName(props.getProperty("host"));
			f.port = Integer.parseInt(props.getProperty("port"));
			f.connectTo = f.addr.getHostAddress() + ":" + f.port;
                        f.connect();
		} catch(IOException e){
			JOptionPane.showMessageDialog(null, "While connecting to " + f.connectTo + "\n" + e);
			System.exit(1);
		}
		
		new Thread(f).start();
		f.setVisible(true);
		}
}