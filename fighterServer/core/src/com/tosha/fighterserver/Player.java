/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tosha.fighterserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * @author Aantokhin
 */
public class Player {
    private InetAddress address;
    private int port;
    private Socket clientSocket;
    private DataInputStream inStream;
    private DataOutputStream outStream;

    public Player(Socket clientSocket, DataInputStream inStream, DataOutputStream outStream) {
        this.clientSocket = clientSocket;
        this.inStream = inStream;
        this.outStream = outStream;
        this.address = clientSocket.getInetAddress();
        this.port = (int) (Math.random() * 500) + 5100; //assign new random port for UDP communications
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public DataInputStream getInStream() {
        return inStream;
    }

    public void setInStream(DataInputStream inStream) {
        this.inStream = inStream;
    }

    public DataOutputStream getOutStream() {
        return outStream;
    }

    public void setOutStream(DataOutputStream outStream) {
        this.outStream = outStream;
    }
    
    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    
    
}
