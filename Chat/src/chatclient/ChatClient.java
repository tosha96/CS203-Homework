/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import chatframework.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author Aantokhin
 */
public class ChatClient {

    JTextArea incoming;
    JTextField outgoing;
    JTextField setName;
    ObjectInputStream reader;
    ObjectOutputStream writer;
    Socket sock;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.go();
    }
    
    public void go() {
        JFrame frame = new JFrame("Chat Client");
        JPanel mainPanel = new JPanel();
        JPanel inputPanel = new JPanel();
        JPanel namePanel = new JPanel();
        incoming = new JTextArea(15,10);
        incoming.setLineWrap(true);
        incoming.setEditable(false);
        JScrollPane qScroller = new JScrollPane(incoming);
        qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        outgoing = new JTextField(20);
        setName = new JTextField(20);
        JButton sendButton = new JButton("Send");
        JButton setNameButton = new JButton("Set Name");
        sendButton.addActionListener(new SendButtonListener());
        setNameButton.addActionListener(new SetNameButtonListener());
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(qScroller);
        inputPanel.add(outgoing);
        inputPanel.add(sendButton);
        namePanel.add(setName);
        namePanel.add(setNameButton);
        mainPanel.add(inputPanel);
        mainPanel.add(namePanel);
        
        setUpNetworking();
        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        frame.setSize(800,500);
        frame.setVisible(true);
    }
    
    private void setUpNetworking() {
        try {
            sock = new Socket("127.0.0.1", 5000);
            reader = new ObjectInputStream(sock.getInputStream());
            writer = new ObjectOutputStream(sock.getOutputStream());
            System.out.println("Networking established.");
        } catch (IOException ex)  { ex.printStackTrace(); }
        
    }
        
    public class SendButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            try {
                Message msg = new Message(outgoing.getText(), null, "mainroom");
                writer.writeObject(msg);
                writer.flush();
            } catch (Exception ex)  { ex.printStackTrace(); }
            outgoing.setText("");
            outgoing.requestFocus();
        }
    }
    
    public class SetNameButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            try {
                Message msg = new Message(setName.getText(), null, "setName");
                writer.writeObject(msg);
                writer.flush();
            } catch (Exception ex)  { ex.printStackTrace(); }
        }  
    }
    
    public class IncomingReader implements Runnable {
        @Override
        public void run() {
            Message message;
            try {
                while ((message = (Message) reader.readObject()) != null) {
                    //message = (Message) reader.readObject();
                    //System.out.println("read " + message.getContent());
                    if (message.getDestination().equals("mainroom") || message.getDestination().equals("all")) {
                        incoming.append(message.getUser() + ": " + message.getContent() + "\n");
                    }
                }
            } catch (Exception ex)  { ex.printStackTrace(); }
        }
    }
    
}
