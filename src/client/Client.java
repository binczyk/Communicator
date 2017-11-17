/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author Mariusz
 */
public class Client extends JFrame implements ActionListener, KeyListener, WindowListener, Runnable {

	private final JTextField tf;
	private final JScrollPane skr;
	private final JTextArea panelg;
	private final JButton bok;
	private PrintWriter out = null;
	private BufferedReader in = null;

	public Client(String title){
	 	super(title);
		setSize(500,400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container interior=getContentPane();
		interior.setLayout(new BorderLayout());
		panelg=new JTextArea();
		panelg.setEditable(false);
		skr=new JScrollPane(panelg);
		interior.add(skr, BorderLayout.CENTER);
		JPanel paneld=new JPanel();
		paneld.setLayout(new BorderLayout());
		tf=new JTextField();
		paneld.add(tf, BorderLayout.CENTER);
		bok=new JButton("OK");
		bok.addActionListener(this);
		tf.addKeyListener(this);
		paneld.add (bok, BorderLayout.EAST);
		interior.add (paneld, BorderLayout.SOUTH);
		addWindowListener(this);
		Dimension dim = getToolkit().getScreenSize();
		Rectangle abounds = getBounds();
		setLocation((dim.width - abounds.width) / 2, (dim.height - abounds.height) / 2);
	}

        @Override
	public void keyReleased(KeyEvent e) {}
	
        @Override
	public void keyPressed(KeyEvent e) {
		if( e.getKeyCode() == KeyEvent.VK_ENTER ){
			bok.doClick();
		}
	}

        @Override
	public void keyTyped(KeyEvent e) {}
	
        @Override
	public void windowOpened(WindowEvent e) {
		tf.requestFocus();
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
		String s = tf.getText();
		if(s.equals("")) return;
		try {
			out.println(s);
		} catch(Exception e) { JOptionPane.showMessageDialog(null, e); System.exit(0); }
		tf.setText(null);	
	}
		
        @Override
	public void run() {
		for(;;) {
			try {
				String s=in.readLine();
				if(s == null) {
					JOptionPane.showMessageDialog(null, "Connection closed by the server");
					System.exit(0);
				}
				panelg.append(s+"\n");
				skr.getVerticalScrollBar().setValue(skr.getVerticalScrollBar().getMaximum());
			} catch(Exception e) { JOptionPane.showMessageDialog(null, e); System.exit(0); }
		}
	}
	
	public static void main (String[] args) {
		Client f=new Client("Communicator client");
		
		String connectTo=null;
		try{
			Properties props = new Properties();
			props.load(new FileInputStream("Client.properties"));
			InetAddress addr=InetAddress.getByName(props.getProperty("host"));
			int port=Integer.parseInt(props.getProperty("port"));
			connectTo=addr.getHostAddress()+":"+port;
			Socket sock=new Socket (addr.getHostName(), port);
			f.setTitle("Connected to "+connectTo);
			f.out=new PrintWriter(sock.getOutputStream(), true);
			f.in=new BufferedReader(new InputStreamReader(sock.getInputStream()));
		} catch(Exception e){
			JOptionPane.showMessageDialog(null, "While connecting to " + connectTo + "\n" + e);
			System.exit(1);
		}
		
		new Thread(f).start();
		f.setVisible(true);
		}
}