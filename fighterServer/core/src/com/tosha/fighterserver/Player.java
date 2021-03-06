/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tosha.fighterserver;

import static com.tosha.fighterserver.FighterServer.protocolID;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.BitSet;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author Aantokhin
 */
public class Player {

    private InetAddress address;
    private int port;
    private Socket clientSocket;
    private DatagramSocket udpSocket;
    private DataInputStream inStream;
    private DataOutputStream outStream;
    int sequence = 0;
    int ack = 0;
    boolean[] localAckSet = new boolean[32];
    boolean[] remoteAckSet = new boolean[32];
    ArrayBlockingQueue<PacketContainer> inPackets = new ArrayBlockingQueue<PacketContainer>(33);
    ArrayBlockingQueue<PacketContainer> outPackets = new ArrayBlockingQueue<PacketContainer>(33);

    ByteBuffer wrapped;
    byte[] buffer;
    DatagramPacket packet;
    InputState state;


    public Player(Socket clientSocket, DataInputStream inStream, DataOutputStream outStream) {
        try {
            this.clientSocket = clientSocket;
            this.inStream = inStream;
            this.outStream = outStream;
            this.address = clientSocket.getInetAddress();
            this.port = (int) (Math.random() * 500) + 5100; //assign new random port for UDP communications
            this.udpSocket = new DatagramSocket(this.port);
            state = new InputState();
        } catch (Exception ex) {
        }
    }

    public DatagramSocket getUdpSocket() {
        return udpSocket;
    }

    public void setUdpSocket(DatagramSocket udpSocket) {
        this.udpSocket = udpSocket;
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

    public synchronized void updateState(float x1, float y1, float xv1, float yv1, 
            float x2, float y2, float xv2, float yv2, int headingLeft1, int headingLeft2, 
            int onGround1, int onGround2) {
        try {
            buffer = new byte[256];
            wrapped = ByteBuffer.wrap(buffer);
            //packet = new DatagramPacket(buffer, buffer.length);

            wrapped.position(0);
            wrapped.putInt(protocolID);
            wrapped.position(4);
            wrapped.putInt(this.sequence);
            wrapped.position(8);
            wrapped.putInt(this.ack);
            wrapped.position(12);
            BitSet tempBitSet = new BitSet();
            for (int i = 0; i < this.localAckSet.length; i++) {
                tempBitSet.set(i, this.localAckSet[i]); //convert from boolean[] to bitset
            }
            wrapped.put(tempBitSet.toByteArray()); //its okay to write the whole bitset because the bitset > 32 is just zeroes
            wrapped.position(16);
            
            wrapped.putInt(0); //id for game state update
            wrapped.position(20);
            
            wrapped.putFloat(x1);
            wrapped.position(24);
            wrapped.putFloat(y1);
            wrapped.position(28);
            wrapped.putFloat(xv1);
            wrapped.position(32);
            wrapped.putFloat(yv1);
            wrapped.position(36);
            
            wrapped.putFloat(x2);
            wrapped.position(40);
            wrapped.putFloat(y2);
            wrapped.position(44);
            wrapped.putFloat(xv2);
            wrapped.position(48);
            wrapped.putFloat(yv2);
            wrapped.position(52);
            
            wrapped.putInt(headingLeft1);
            wrapped.position(56);
            wrapped.putInt(headingLeft2);
            
            wrapped.position(60);
            wrapped.putInt(onGround1);
            wrapped.position(64);
            wrapped.putInt(onGround2);
            

            //byte[] echo = "echo".getBytes();
            //wrapped.put(echo);

            packet = new DatagramPacket(buffer, buffer.length, address, port);

            if (!this.outPackets.isEmpty()) {
                this.outPackets.take(); //remove oldest packet from recieved packet queue
            }
            this.outPackets.add(new PacketContainer(packet, Instant.now().getEpochSecond())); //need to fix like inpackets

            udpSocket.send(packet);
            this.sequence += 1;
            //System.out.println(address.toString() + " " + port);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
