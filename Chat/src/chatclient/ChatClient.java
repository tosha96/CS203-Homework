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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Aantokhin
 */
public class ChatClient {

    JMenuBar menuBar;
    JMenu userMenu, roomMenu, prefMenu;
    JMenuItem menuItem;

    JFrame frame;
    JTextPane incoming;
    JScrollPane qScroller;
    JList userList;
    JList roomList;
    JTextField outgoing;
    JTextField setName;
    ObjectInputStream reader;
    ObjectOutputStream writer;
    Socket sock;
    DefaultListModel usersModel = new DefaultListModel();
    DefaultListModel<RoomObject> roomsModel = new DefaultListModel();
    JPanel noWrapPanel;

    int currentRoom = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.go();
    }

    ListSelectionListener roomListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent lse) {
            if (!lse.getValueIsAdjusting()) {
                //RoomObject room = (RoomObject) roomList.getSelectedValue();
                int roomIndex = roomList.getMinSelectionIndex();
                try {
                    incoming.setText(roomsModel.get(roomIndex).getText());

                    updateRoomUsers(roomIndex);

                    currentRoom = roomIndex;
                    incoming.repaint();
                    userList.repaint();
                } catch (Exception ex) {
                }
            }
        }
    };

    public void go() {
        frame = new JFrame("Chat Client");
        JPanel mainPanel = new JPanel();
        JPanel inputPanel = new JPanel();
        JPanel namePanel = new JPanel();
        noWrapPanel = new JPanel(new BorderLayout());

        menuBar = new JMenuBar();
        roomMenu = new JMenu("Room");
        userMenu = new JMenu("User");
        prefMenu = new JMenu("Preferences");
        menuBar.add(roomMenu);
        menuBar.add(userMenu);
        menuBar.add(prefMenu);
        menuItem = new JMenuItem("Leave room");
        menuItem.addActionListener(new MenuListener());
        roomMenu.add(menuItem);
        menuItem = new JMenuItem("Join new room");
        menuItem.addActionListener(new MenuListener());
        roomMenu.add(menuItem);
        menuItem = new JMenuItem("Room settings");
        menuItem.addActionListener(new MenuListener());
        roomMenu.add(menuItem);
        menuItem = new JMenuItem("Set name");
        menuItem.addActionListener(new MenuListener());
        userMenu.add(menuItem);
        frame.setJMenuBar(menuBar);

        roomsModel.addElement(new RoomObject("mainroom"));
        roomsModel.addElement(new RoomObject("testroom"));

        userList = new JList(usersModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setLayoutOrientation(JList.VERTICAL);
        userList.setVisibleRowCount(-1);

        roomList = new JList(roomsModel);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomList.setLayoutOrientation(JList.VERTICAL);
        roomList.setVisibleRowCount(-1);
        roomList.setSelectedIndex(0);
        roomList.addListSelectionListener(roomListener);

        JScrollPane userScroller = new JScrollPane(userList);
        userScroller.setPreferredSize(new Dimension(100, 500));

        JScrollPane roomScroller = new JScrollPane(roomList);
        roomScroller.setPreferredSize(new Dimension(100, 500));

        incoming = new JTextPane();
        //incoming.setLineWrap(true);
        incoming.setEditable(false);
        incoming.setText(roomsModel.get(currentRoom).getText());
        noWrapPanel.add(incoming);

        qScroller = new JScrollPane(noWrapPanel);
        qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        qScroller.setViewportView(incoming);

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
                Message msg = new Message(outgoing.getText(), null, roomsModel.get(currentRoom).getName(), "message");
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
        roomsModel.get(currentRoom).setText(roomsModel.get(currentRoom).getText() + content);
        incoming.setText(roomsModel.get(currentRoom).getText());
        incoming.repaint();
    }

    public void updateRoomUsers(int index) {
        //index is the index of the room we are updating
        usersModel.clear();
        for (Object roomUserModel : roomsModel.get(index).getUsersModel().toArray()) {
            usersModel.addElement(roomUserModel);
        }
    }

    public class SetNameButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ev) {
            try {
                Message msg = new Message(setName.getText(), null, "setName", "setName");
                writer.writeObject(msg);
                writer.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public class MenuListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ev) {
            try {
                switch (ev.getActionCommand()) {
                    case "Leave room":
                        Message msg = new Message("leave", null, roomsModel.get(currentRoom).getName(), "roomState");
                        writer.writeObject(msg);
                        writer.flush();
                        break;
                    case "Join new room":
                        RoomEntry roomEntry = new RoomEntry();
                        roomEntry.joinButton.addActionListener(new JoinButtonListener(roomEntry));
                        break;
                }
                System.out.println(ev.getActionCommand());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public class JoinButtonListener implements ActionListener {
        RoomEntry roomEntry;
        
        //let it know which roomentry object to listen on
        public JoinButtonListener (RoomEntry roomEntry) {
            this.roomEntry = roomEntry;
        }
        
        @Override
        public void actionPerformed(ActionEvent ae) {
            try {
                Message msg = new Message("join", null, roomEntry.roomName.getText(), "roomState");
                writer.writeObject(msg);
                writer.flush();
                roomEntry.dispatchEvent(new WindowEvent(roomEntry, WindowEvent.WINDOW_CLOSING));
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
                    switch (message.getType()) {
                        case "message":
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
                                        //roomsModel.addElement(room);
                                    }
                                    break;
                                default:
                                    //go through rooms and give them their appropriate messages
                                    for (int i = 0; i < roomsModel.size(); i++) {
                                        if (roomsModel.get(i).getName().equals(message.getDestination())) {
                                            roomsModel.get(i).setText(roomsModel.get(i).getText() + message.getUser() + ": " + message.getContent() + "\n");
                                            if (currentRoom == i) {
                                                incoming.setText(roomsModel.get(i).getText());
                                                incoming.repaint();
                                            }
                                        }
                                    }
                                    break;
                            }
                            break;
                        case "userList":
                            tokens = message.getContent().split(delims);
                            DefaultListModel model = new DefaultListModel();
                            model.clear();
                            for (String user : tokens) {
                                model.addElement(user);
                            }
                            for (int i = 0; i < roomsModel.size(); i++) {
                                //find the right room to update list for
                                if (roomsModel.get(i).getName().equals(message.getDestination())) {
                                    roomsModel.get(i).setUsersModel(model);
                                }
                            }
                            updateRoomUsers(currentRoom);
                            break;
                        case "roomState":
                            if (message.getContent().equals("leave")) {
                                System.out.println("test");
                                for (int i = 0; i < roomsModel.size(); i++) {
                                    //find the right room to update list for
                                    if (roomsModel.get(i).getName().equals(message.getDestination())) {
                                        //copy old room list
                                        DefaultListModel<RoomObject> oldModel = new DefaultListModel();
                                        for (Object room : roomsModel.toArray()) {
                                            oldModel.addElement((RoomObject) room);
                                        }
                                        roomsModel.clear();
                                        for (int j = 0; j < oldModel.size(); j++) {
                                            if (j != i) {
                                                roomsModel.addElement(oldModel.elementAt(j));
                                            }
                                        }
                                        //roomsModel.removeElementAt(i);
                                        
                                        roomList.setSelectedIndex(0);
                                        
                                        //incoming.setText(roomsModel.get(currentRoom).getText());
                                        //incoming.repaint();
                                        //updateRoomUsers(currentRoom);
                                        //System.out.println("test");
                                        break;
                                    }
                                }
                            } else if (message.getContent().equals("join")) {
                                RoomObject room = new RoomObject(message.getDestination());
                                roomsModel.addElement(room);
                                //set focus to new room
                                roomList.setSelectedIndex(roomsModel.lastIndexOf(room));
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
