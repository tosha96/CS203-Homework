/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import chatframework.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author Aantokhin
 */
public class ChatServer {

    Vector<User> users = new Vector<>();

    public class ClientHandler implements Runnable {

        int userIndex;

        public ClientHandler(int userIndex) {
            try {
                this.userIndex = userIndex;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void run() {
            Message message;
            try {
                while ((message = (Message) users.get(userIndex).inputStream.readObject()) != null) {
                    message.setUser(users.get(userIndex).getUsername());
                    if (message.getDestination().equals("setName")) {
                        setName(userIndex, message.getContent());
                    } else {
                        if (message.getUser().equals(users.get(userIndex).getUsername())) {
                            broadcastMessage(message);
                        }
                    }
                }
            } catch (SocketException ex) { //handles disconnected users
                broadcastMessage(new Message(users.get(userIndex).getUsername() 
                        + " disconnected: " + ex.getMessage(), "Server", "mainroom"));
                users.set(userIndex, null); //set disconnected users in array as null
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new ChatServer().go();
    }

    public void go() {
        try {
            ServerSocket serverSock = new ServerSocket(5000);

            while (true) {
                Socket clientSocket = serverSock.accept();
                //set up client input and output streams in new user objects
                ObjectOutputStream writer = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream reader = new ObjectInputStream(clientSocket.getInputStream());
                User newUser = new User("user" + (users.size() + 1), writer, reader, clientSocket);
                //set default name with user index
                users.add(newUser);
                

                Thread t = new Thread(new ClientHandler(users.indexOf(newUser)));
                t.start();
                System.out.println("Got a connection.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void broadcastMessage(Message message) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i) != null) {
                if (users.get(i).rooms.contains(message.getDestination())) {
                    try {
                        users.get(i).outputStream.writeObject(message);
                        users.get(i).outputStream.flush();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public synchronized void sendMessage(int userIndex, Message message) {
        if (users.get(userIndex) != null) {
            try {
                users.get(userIndex).outputStream.writeObject(message);
                users.get(userIndex).outputStream.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public synchronized void setName(int userIndex, String name) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i) != null) {
                if (users.get(i).getUsername().equals(name) && i != userIndex) {
                    sendMessage(userIndex, new Message("Name " + name + " is already taken.",
                            "Server", "all"));
                    return;
                }
            }
        }
        users.get(userIndex).setUsername(name);
    }
}
