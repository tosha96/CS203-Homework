/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

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
    BufferedReader reader;
    PrintWriter writer;
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
        incoming = new JTextArea(15,10);
        
    }
    
}
