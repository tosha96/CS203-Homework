package com.tosha.fighterserver;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.net.*;
import java.nio.*;
import java.util.BitSet;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

public class FighterServer {

    static final int protocolID = 68742731;
    Vector<Player> players = new Vector<Player>();

    DatagramSocket socket;
    byte[] buffer = new byte[256];
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    InetAddress address;
    ByteBuffer wrapped;
    int sequence = 0;
    int ack = 0;
    boolean[] localAckSet = new boolean[32];
    boolean[] remoteAckSet = new boolean[32];
    ArrayBlockingQueue<DatagramPacket> inPackets = new ArrayBlockingQueue<DatagramPacket>(33);
    ArrayBlockingQueue<DatagramPacket> outPackets = new ArrayBlockingQueue<DatagramPacket>(33);
    int port;
    int remoteAck;
    byte[] remoteAckBytes = new byte[4];
    int remoteSequence; // the server's remote sequence int that we recieve
    int sequenceDifference; // the difference between the remote and local sequence
    int bitIndex; // the index in the bitfield for the packet we are going to set
    boolean[] tmpSet;

    public static void main(String[] arg) {
        FighterServer server = new FighterServer();
        server.go();
    }

    public void go() {
        try {
            socket = new DatagramSocket(4445);
                
            while (true) {
                //
                //Code to recieve packet and update ack info
                //
                socket.receive(packet);
                //buffer = packet.getData();
                wrapped = ByteBuffer.wrap(packet.getData());
                wrapped.position(0);
                if (wrapped.getInt() == protocolID) {
                    wrapped.position(wrapped.position() + 4);
                    remoteSequence = wrapped.getInt();
                    if (remoteSequence >= ack - 32) { //make sure packet is recent enough to matter
                            /*if (!inPackets.isEmpty()) {
                         inPackets.take(); //remove oldest packet from recieved packet queue
                         }
                         inPackets.add(packet); //add newest packet to queue*/

                        if (remoteSequence > ack) {
                            sequenceDifference = remoteSequence - ack;
                            if (sequenceDifference > 32) {
                                localAckSet = new boolean[32];
                                ack = remoteSequence;
                            } else {
                                tmpSet = localAckSet; //copy localAckSet
                                localAckSet = new boolean[32];
                                for (int i = sequenceDifference; i < 32 - sequenceDifference; i++) {
                                    //i - sequenceDifference = the start of the bitset
                                    localAckSet[i - sequenceDifference] = tmpSet[i];
                                }
                                localAckSet[32 - sequenceDifference] = true; // push value for last local ack into bitset
                                ack = remoteSequence; //update local ack if remote sequence is more recent
                            }
                        } else {
                            bitIndex = 31 - (ack - remoteSequence); //get index of bit to be set
                            if (!localAckSet[bitIndex]) {
                                localAckSet[bitIndex] = true;
                            }
                        }
                        wrapped.position(wrapped.position() + 4);
                        remoteAck = wrapped.getInt(); //get remote ack

                        wrapped.position(wrapped.position() + 4);
                        wrapped.get(remoteAckBytes); //get remote ack bitset
                        
                        for (int i = 0; i < remoteAckBytes.length; i++) {
                            for (int j = 0; j <= 7; j++) {
                                int bitSetIndex = (i * 7) + i + j; //generates numbers 0-32 for setting bitset values
                                if (bitSetIndex == 32) {
                                    break;
                                }
                                remoteAckSet[bitSetIndex] = isBitSet(remoteAckBytes[i], j);
                            }
                        }
                    }
                    //String received = new String(inBytes, 0, inBytes.length);
                    System.out.println("Local Sequence: " + sequence);
                    System.out.println("Remote Sequence: " + ack);
                    for (int i = localAckSet.length - 1; i >= 0; i--) {
                        System.out.println(i + " " + localAckSet[i]);
                    }
                    //
                    //Code to send packet
                    //

                    buffer = new byte[256];
                    wrapped = ByteBuffer.wrap(buffer);

                    wrapped.putInt(protocolID);
                    wrapped.position(wrapped.position() + 4);
                    wrapped.putInt(sequence);
                    wrapped.position(wrapped.position() + 4);
                    wrapped.putInt(ack);
                    wrapped.position(wrapped.position() + 4);
                    BitSet tempBitSet = new BitSet();
                    for (int i = 0; i < localAckSet.length; i++) {
                        tempBitSet.set(i, localAckSet[i]); //convert from boolean[] to bitset
                    }
                    wrapped.put(tempBitSet.toByteArray()); //its okay to write the whole bitset because the bitset > 32 is just zeroes
                    wrapped.position(wrapped.position() + 4);

                    byte[] echo = "echo".getBytes();
                    wrapped.put(echo);

                    address = packet.getAddress();
                    port = packet.getPort();
                    packet = new DatagramPacket(buffer, buffer.length, address, port);
                    //outPackets.add(packet); //need to fix like inpackets
                    socket.send(packet);
                    sequence += 1;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //currently unused multithreading code
    public class ClientHandler implements Runnable {

        Player player;

        public ClientHandler(Player player) {
            try {
                this.player = player;
            } catch (Exception ex) {
            }
        }

        @Override
        public void run() {
            try {
                DatagramSocket socket = new DatagramSocket(player.getPort());
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                ByteBuffer wrapped;

                while (true) {
                    socket.receive(packet);
                    buffer = packet.getData();
                    wrapped = ByteBuffer.wrap(buffer);
                    if (wrapped.getInt(0) == protocolID) {

                        //System.out.println(new String(buffer, 0, buffer.length));
                        //InetAddress address = packet.getAddress();
                        //int port = packet.getPort();
                        packet = new DatagramPacket(buffer, buffer.length, player.getAddress(), player.getPort());
                        socket.send(packet);
                    }
                }
            } catch (Exception ex) {
            }
        }

    }

    private static Boolean isBitSet(byte b, int bit) {
        return (b & (1 << bit)) != 0;
    }

}
