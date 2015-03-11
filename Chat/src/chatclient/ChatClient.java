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

    JFrame frame;
    JTextArea incoming;
    JScrollPane qScroller;
    JList userList;
    JList roomList;
    JTextField outgoing;
    JTextField setName;
    ObjectInputStream reader;
    ObjectOutputStream writer;
    Socket sock;
    DefaultListModel usersModel = new DefaultListModel();
    DefaultListModel roomsModel = new DefaultListModel();

    ArrayList<RoomObject> roomObjects = new ArrayList<>();
    int currentRoom = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.go();
    }

    MouseListener roomMouseListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1) {
                int index = roomList.locationToIndex(e.getPoint());
                //System.out.println("Single clicked on Item " + index);
                String roomName = (String) roomsModel.elementAt(index);
                for (int i = 0; i < roomObjects.size(); i++) {
                    if (roomObjects.get(i).getName().equals(roomName)) {
                        incoming.setText(roomObjects.get(i).getText());
                        currentRoom = i;
                        incoming.repaint();
                    }
                }
            }
        }
    };

    public void go() {
        frame = new JFrame("Chat Client");
        JPanel mainPanel = new JPanel();
        JPanel inputPanel = new JPanel();
        JPanel namePanel = new JPanel();

        roomObjects.add(new RoomObject("mainroom"));
        roomObjects.add(new RoomObject("testroom"));
        roomsModel.addElement("mainroom");
        roomsModel.addElement("testroom");

        userList = new JList(usersModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        userList.setLayoutOrientation(JList.VERTICAL);
        userList.setVisibleRowCount(-1);

        roomList = new JList(roomsModel);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        roomList.setLayoutOrientation(JList.VERTICAL);
        roomList.setVisibleRowCount(-1);
        roomList.addMouseListener(roomMouseListener);

        JScrollPane userScroller = new JScrollPane(userList);
        userScroller.setPreferredSize(new Dimension(100, 500));

        JScrollPane roomScroller = new JScrollPane(roomList);
        roomScroller.setPreferredSize(new Dimension(100, 500));

        incoming = new JTextArea(15, 10);
        incoming.setLineWrap(true);
        incoming.setEditable(false);
        incoming.setText(roomObjects.get(currentRoom).getText());

        qScroller = new JScrollPane(incoming);
        qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        outgoing = new JTextField(20);
        setName = new JTextField(10);
        JButton sendButton = new JButton("Send");
        JButton setNameButton = new JButton("Set Name");
        sendButton.addActionListener(new SendButtonListener());
        setNameButton.addActionListener(new SetNameButtonListener());

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(qScroller);

        namePanel.add(setName);
        namePanel.add(setNameButton);

        inputPanel.add(namePanel);
        inputPanel.add(outgoing);
        inputPanel.add(sendButton);

        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        frame.getContentPane().add(BorderLayout.EAST, userScroller);
        frame.getContentPane().add(BorderLayout.WEST, roomScroller);
        frame.getContentPane().add(BorderLayout.SOUTH, inputPanel);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setVisible(true);

        while (!setUpNetworking()) {
            try {

                addToCurrentRoom("Connection failed, retrying..." + "\n");
                Thread.sleep(1500);
            } catch (Exception ex) {
            }
        }

        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();
    }

    private boolean setUpNetworking() {
        try {
            sock = new Socket("127.0.0.1", 5000);
            reader = new ObjectInputStream(sock.getInputStream());
            writer = new ObjectOutputStream(sock.getOutputStream());
            addToCurrentRoom("Connected!" + "\n");
            return true;
        } catch (IOException ex) {
            //ex.printStackTrace();
            return false;
        }

    }

    public class SendButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ev) {
            try {
                Message msg = new Message(outgoing.getText(), null, roomObjects.get(currentRoom).getName(), "message");
                writer.writeObject(msg);
                writer.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            outgoing.setText("");
            outgoing.requestFocus();
        }
    }

    public void addToCurrentRoom(String content) {
        roomObjects.get(currentRoom).setText(roomObjects.get(currentRoom).getText() + content);
        incoming.setText(roomObjects.get(currentRoom).getText());
        incoming.repaint();
    }

    public class SetNameButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ev) {
            try {
                Message msg = new Message(setName.getText(), null, "setName", "message");
                writer.writeObject(msg);
                writer.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public class IncomingReader implements Runnable {

        @Override
        public void run() {
            Message message;
            try {
                while ((message = (Message) reader.readObject()) != null) {
                    //variables for room/user message tokenization
                    String delims = "[,]";
                    String[] tokens = new String[0];
                    if (message.getType().equals("message")) {
                        switch (message.getDestination()) {
                            case "current":
                                addToCurrentRoom(message.getUser() + ": " + message.getContent() + "\n");
                                incoming.repaint();
                                break;
                            case "roomList":
                                //update list of rooms
                                tokens = message.getContent().split(delims);
                                roomsModel.clear();
                                for (String room : tokens) {
                                    roomsModel.addElement(room);
                                }
                                break;
                            default:
                                //go through rooms and give them their appropriate messages
                                for (RoomObject room : roomObjects) {
                                    if (room.getName().equals(message.getDestination())) {
                                        room.setText(room.getText() + message.getUser() + ": " + message.getContent() + "\n");
                                        incoming.setText(room.getText());
                                        incoming.repaint();
                                    }
                                }
                                break;
                        }
                    } else if (message.getType().equals("userList")) {
                        tokens = message.getContent().split(delims);
                        usersModel.clear();
                        for (String user : tokens) {
                            usersModel.addElement(user);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
