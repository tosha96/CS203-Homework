package com.tosha.fighterserver;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.nio.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

// message types:
// 0 = game state update
// 1 = input from player
//

public class FighterServer {

    static final int protocolID = 68742731;
    Vector<Player> players = new Vector<Player>();

    World world;
    ArrayList<Body> bodies = new ArrayList<Body>();
    Body groundBody;

    public static void main(String[] arg) {
        FighterServer server = new FighterServer();
        server.go();
    }

    public void go() {
        try {
            Thread connectionHandler = new Thread(new ConnectionHandler());
            connectionHandler.start();

            Box2D.init();
            world = new World(new Vector2(0, -20), true);

            BodyDef groundBodyDef = new BodyDef();
            groundBodyDef.position.set(new Vector2(0, 10));

            groundBody = world.createBody(groundBodyDef);

            PolygonShape groundBox = new PolygonShape();
            groundBox.setAsBox(900.0f, 10.0f);
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = groundBox;
            fixtureDef.density = 0.0f;
            groundBody.createFixture(fixtureDef);
            groundBody.setUserData(new BodyData("ground", groundBody.getPosition().x, groundBody.getPosition().y));
            groundBox.dispose();

            while (true) {
                Thread.sleep(33);
                //gameworld update logic here
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public class ClientHandler implements Runnable {

        Player player;

        DatagramSocket socket;
        byte[] buffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        InetAddress address;
        ByteBuffer wrapped;
        //int sequence = 0;
        //int ack = 0;
        //boolean[] localAckSet = new boolean[32];
        //boolean[] remoteAckSet = new boolean[32];
        //ArrayBlockingQueue<PacketContainer> inPackets = new ArrayBlockingQueue<PacketContainer>(33);
        //ArrayBlockingQueue<PacketContainer> outPackets = new ArrayBlockingQueue<PacketContainer>(33);
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
                socket = player.getUdpSocket();

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
                        if (remoteSequence >= player.ack - 32) { //make sure packet is recent enough to matter
                            if (!player.inPackets.isEmpty()) {
                                player.inPackets.take(); //remove oldest packet from recieved packet queue
                            }
                            player.inPackets.add(new PacketContainer(packet, Instant.now().getEpochSecond())); //add newest packet to queue

                            if (remoteSequence > player.ack) {
                                sequenceDifference = remoteSequence - player.ack;
                                if (sequenceDifference > 32) {
                                    player.localAckSet = new boolean[32];
                                    player.ack = remoteSequence;
                                } else {
                                    tmpSet = Arrays.copyOfRange(player.localAckSet, sequenceDifference, 31); //copy localAckSet
                                    player.localAckSet = new boolean[32];
                                    for (int i = 0; i < tmpSet.length; i++) {
                                        player.localAckSet[i] = tmpSet[i];
                                    }

                                    player.localAckSet[31 - sequenceDifference] = true; // push value for last local ack into bitset
                                    player.ack = remoteSequence; //update local ack if remote sequence is more recent
                                }
                            } else {
                                bitIndex = 31 - (player.ack - remoteSequence); //get index of bit to be set
                                if (!player.localAckSet[bitIndex]) {
                                    player.localAckSet[bitIndex] = true;
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
                                    player.remoteAckSet[bitSetIndex] = isBitSet(remoteAckBytes[i], j);
                                }
                            }
                        }
                        //String received = new String(inBytes, 0, inBytes.length);
                        System.out.println("Local Sequence: " + player.sequence);
                        System.out.println("Remote Sequence: " + player.ack);
                        for (int i = player.remoteAckSet.length - 1; i >= 0; i--) {
                            System.out.println(i + " " + player.remoteAckSet[i]);
                        }

                        //resend lost packets
                        long now = Instant.now().getEpochSecond();
                        for (PacketContainer pc : player.outPackets) {
                            wrapped = ByteBuffer.wrap(pc.getPacket().getData());
                            wrapped.position(4);
                            int sequenceNumber = wrapped.getInt();
                            if (sequenceNumber != player.ack) {
                                //check to see if packed is acked in ackset
                                //resend if it isn't
                                int ackIndex = player.ack - sequenceNumber;

                                if (ackIndex >= 0 && ackIndex < 32) {
                                    if (!player.remoteAckSet[31 - (player.ack - sequenceNumber)] && now - pc.getTimestamp() >= 1) {
                                        socket.send(pc.getPacket());
                                        System.out.println("resending packet");
                                    }
                                }
                            } else {
                                if (now - pc.getTimestamp() >= 1) {
                                    socket.send(pc.getPacket());
                                    System.out.println("resending packet");
                                }
                            }
                        }
                        
                        //
                        //Code to send packet
                        //
                        /*
                        buffer = new byte[256];
                        wrapped = ByteBuffer.wrap(buffer);

                        wrapped.putInt(protocolID);
                        wrapped.position(wrapped.position() + 4);
                        wrapped.putInt(player.sequence);
                        wrapped.position(wrapped.position() + 4);
                        wrapped.putInt(player.ack);
                        wrapped.position(wrapped.position() + 4);
                        BitSet tempBitSet = new BitSet();
                        for (int i = 0; i < player.localAckSet.length; i++) {
                            tempBitSet.set(i, player.localAckSet[i]); //convert from boolean[] to bitset
                        }
                        wrapped.put(tempBitSet.toByteArray()); //its okay to write the whole bitset because the bitset > 32 is just zeroes
                        wrapped.position(wrapped.position() + 4);

                        byte[] echo = "echo".getBytes();
                        wrapped.put(echo);

                        address = packet.getAddress();
                        port = packet.getPort();
                        packet = new DatagramPacket(buffer, buffer.length, address, port);

                        if (!player.outPackets.isEmpty()) {
                            player.outPackets.take(); //remove oldest packet from recieved packet queue
                        }
                        player.outPackets.add(new PacketContainer(packet, Instant.now().getEpochSecond())); //need to fix like inpackets

                        socket.send(packet);
                        player.sequence += 1;
                        */
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public class ConnectionHandler implements Runnable {

        @Override
        public void run() {
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

    }

    private static Boolean isBitSet(byte b, int bit) {
        return (b & (1 << bit)) != 0;
    }

}
