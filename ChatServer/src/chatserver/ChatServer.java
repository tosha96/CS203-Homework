/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author Aantokhin
 */
public class ChatServer {
    
    ArrayList<PrintWriter> clientOutputStreams = new ArrayList<>();
    
    public class ClientHandler implements Runnable {
        BufferedReader reader;
        Socket sock;

        public ClientHandler(Socket clientSocket) {
            try {
                sock = clientSocket;
                InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(isReader);
            } catch (Exception ex) {ex.printStackTrace();}
        }
        
        @Override
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println("read " + message);
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
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                clientOutputStreams.add(writer);
                
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
                System.out.println("Got a connection.");
            } 
        } catch (Exception ex) {ex.printStackTrace();}
    }
    
    public void broadcastMessage(String message) {
        for (PrintWriter writer : clientOutputStreams) {
            try {
                writer.println(message);
                writer.flush();
            } catch (Exception ex) {ex.printStackTrace();}
        }
    }
}
