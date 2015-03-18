package com.tosha.fighterserver;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.nio.*;
import java.time.Instant;
import java.util.Arrays;
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
            ServerSocket serverSock = new ServerSocket(5001);

            while (true) {
                Socket clientSocket = serverSock.accept();
                DataOutputStream outStream = new DataOutputStream(clientSocket.getOutputStream());
                DataInputStream inStream = new DataInputStream(clientSocket.getInputStream());
                Player newPlayer = new Player(clientSocket, inStream, outStream);
                players.add(newPlayer);
                byte[] out = new byte[32];
                ByteBuffer wrapped = ByteBuffer.wrap(out);
                wrapped.position(0);
                wrapped.putInt(1); //1 = accepted connection
                wrapped.position(wrapped.position() + 4);
                wrapped.putInt(newPlayer.getPort());
                System.out.println(newPlayer.getPort());
                newPlayer.getOutStream().write(out);
                newPlayer.getOutStream().flush();

                Thread t = new Thread(new ClientHandler(newPlayer));
                t.start();
                System.out.println("new client accepted");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //currently unused multithreading code
    public class ClientHandler implements Runnable {

        Player player;

        DatagramSocket socket;
        byte[] buffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        InetAddress address;
        ByteBuffer wrapped;
        int sequence = 0;
        int ack = 0;
        boolean[] localAckSet = new boolean[32];
        boolean[] remoteAckSet = new boolean[32];
        ArrayBlockingQueue<PacketContainer> inPackets = new ArrayBlockingQueue<PacketContainer>(33);
        ArrayBlockingQueue<PacketContainer> outPackets = new ArrayBlockingQueue<PacketContainer>(33);
        int port;
        int remoteAck;
        byte[] remoteAckBytes = new byte[4];
        int remoteSequence; // the server's remote sequence int that we recieve
        int sequenceDifference; // the difference between the remote and local sequence
        int bitIndex; // the index in the bitfield for the packet we are going to set
        boolean[] tmpSet;

        public ClientHandler(Player player) {
            try {
                this.player = player;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                port = player.getPort();
                address = player.getAddress();
                socket = new DatagramSocket(port);

                while (true) {
                    System.out.println("start of loop");
                    //
                    //Code to recieve packet and update ack info
                    //
                    socket.receive(packet);
                    //buffer = packet.getData();
                    wrapped = ByteBuffer.wrap(packet.getData());
                    wrapped.position(0);
                    if (wrapped.getInt() == protocolID) {
                        System.out.println("packet has correct id");
                        wrapped.position(wrapped.position() + 4);
                        remoteSequence = wrapped.getInt();
                        System.out.println(remoteSequence + " " + (ack - 32));
                        if (remoteSequence >= ack - 32) { //make sure packet is recent enough to matter
                            System.out.println("packet is recent");
                            if (!inPackets.isEmpty()) {
                                inPackets.take(); //remove oldest packet from recieved packet queue
                            }
                            inPackets.add(new PacketContainer(packet, Instant.now().getEpochSecond())); //add newest packet to queue

                            if (remoteSequence > ack) {
                                sequenceDifference = remoteSequence - ack;
                                if (sequenceDifference > 32) {
                                    localAckSet = new boolean[32];
                                    ack = remoteSequence;
                                } else {
                                    tmpSet = Arrays.copyOfRange(localAckSet, sequenceDifference, 31); //copy localAckSet
                                    localAckSet = new boolean[32];
                                    for (int i = 0; i < tmpSet.length; i++) {
                                        localAckSet[i] = tmpSet[i];
                                    }

                                    localAckSet[31 - sequenceDifference] = true; // push value for last local ack into bitset
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
                        for (int i = remoteAckSet.length - 1; i >= 0; i--) {
                            System.out.println(i + " " + remoteAckSet[i]);
                        }

                        //resend lost packets
                        for (PacketContainer pc : outPackets) {
                            wrapped = ByteBuffer.wrap(pc.getPacket().getData());
                            wrapped.position(4);
                            int sequenceNumber = wrapped.getInt();
                            long now = Instant.now().getEpochSecond();
                            if (sequenceNumber != ack) {
                            //check to see if packed is acked in ackset
                                //resend if it isn't
                                if (!remoteAckSet[31 - (ack - sequenceNumber)] && now - pc.getTimestamp() >= 1) {
                                    socket.send(pc.getPacket());
                                    System.out.println("resending packet");
                                }
                            } else {
                                if (now - pc.getTimestamp() >= 1) {
                                    socket.send(pc.getPacket());
                                    System.out.println("resending packet");
                                }
                            }
                        }
                        
                        System.out.println("checked for lost packed");
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

                        if (!outPackets.isEmpty()) {
                            outPackets.take(); //remove oldest packet from recieved packet queue
                        }
                        outPackets.add(new PacketContainer(packet, Instant.now().getEpochSecond())); //need to fix like inpackets

                        System.out.println("sending packet");
                        socket.send(packet);
                        sequence += 1;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    private static Boolean isBitSet(byte b, int bit) {
        return (b & (1 << bit)) != 0;
    }

}
