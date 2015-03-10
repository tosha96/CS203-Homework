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
                for (RoomObject room : roomObjects) {
                    if (room.getName().equals(roomName)) {
                        incoming = room.incoming;
                    }
                }
            }
        }
    };
    public void go() {
        JFrame frame = new JFrame("Chat Client");
        JPanel mainPanel = new JPanel();
        JPanel inputPanel = new JPanel();
        JPanel namePanel = new JPanel();

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

        JScrollPane qScroller = new JScrollPane(incoming);
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
                incoming.append("Connection failed, retrying..." + "\n");
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
            incoming.append("Connected!" + "\n");
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
                Message msg = new Message(outgoing.getText(), null, "mainroom");
                writer.writeObject(msg);
                writer.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            outgoing.setText("");
            outgoing.requestFocus();
        }
    }

    public class SetNameButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ev) {
            try {
                Message msg = new Message(setName.getText(), null, "setName");
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
                    switch (message.getDestination()) {
                        case "mainroom":
                        case "all":
                            incoming.append(message.getUser() + ": " + message.getContent() + "\n");
                            break;
                        case "userList":
                            //update list of users
                            tokens = message.getContent().split(delims);
                            usersModel.clear();
                            for (String user : tokens) {
                                usersModel.addElement(user);
                            }
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
                            for (RoomObject room : roomObjects) {
                                if (room.getName().equals(message.getDestination())) {
                                    room.incoming.append(message.getUser() + ": " + message.getContent() + "\n");
                                }
                            }
                            break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
