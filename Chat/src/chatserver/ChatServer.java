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
    
    ArrayList<ObjectOutputStream> clientOutputStreams = new ArrayList<>();
    
    public class ClientHandler implements Runnable {
        ObjectInputStream inStream;
        Socket sock;

        public ClientHandler(Socket clientSocket) {
            try {
                sock = clientSocket;
                inStream = new ObjectInputStream(sock.getInputStream());
            } catch (Exception ex) {ex.printStackTrace();}
        }
        
        @Override
        public void run() {
            Message message;
            try {
                while ((message = (Message) inStream.readObject()) != null) {
                    System.out.println("read " + message.getContent() + " from " + message.getUser());
                    broadcastMessage(message);
                }
            } catch (Exception ex) {ex.printStackTrace();}
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
                ObjectOutputStream writer = new ObjectOutputStream(clientSocket.getOutputStream());
                clientOutputStreams.add(writer);
                
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
                System.out.println("Got a connection.");
            } 
        } catch (Exception ex) {ex.printStackTrace();}
    }
    
    public synchronized void broadcastMessage(Message message) {
        for (ObjectOutputStream writer : clientOutputStreams) {
            try {
                writer.writeObject(message);
                writer.flush();
            } catch (Exception ex) {ex.printStackTrace();}
        }
    }
}
