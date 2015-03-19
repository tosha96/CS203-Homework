package com.tosha.fighterserver;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import java.util.concurrent.BlockingQueue;

// message types:
// 0 = game state update
// 1 = input from player
//
public class FighterServer {

    static final int protocolID = 68742731;

    World world;
    Body groundBody;
    PlayerEntity player1;
    PlayerEntity player2;
    ArrayBlockingQueue<InputState> inputs = new ArrayBlockingQueue<InputState>(120);
    boolean player1Connected;

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
            world.setContactListener(new ListenerClass());

            player1 = new PlayerEntity(world, "player1", 100.0f, 100.0f);
            player2 = new PlayerEntity(world, "player2", 400.0f, 100.0f);

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

                Vector2 vel1 = player1.getBody().getLinearVelocity();
                Vector2 pos1 = player1.getBody().getPosition();

                Vector2 vel2 = player2.getBody().getLinearVelocity();
                Vector2 pos2 = player2.getBody().getPosition();

                //player 1 networking
                if (player1.player != null) {
                    if (player1.player.state.isA() && vel1.x > -player1.getMaxVelocityX()) {
                        this.player1.getBody().applyLinearImpulse(-player1.getSpeedX(), 0, pos1.x, pos1.y, true);
                        player1.setHeadingLeft(true);
                    }

                    if (player1.player.state.isD() && vel1.x < player1.getMaxVelocityX()) {
                        this.player1.getBody().applyLinearImpulse(player1.getSpeedX(), 0, pos1.x, pos1.y, true);
                        player1.setHeadingLeft(false);
                    }

                    if (!player1.player.state.isA() && !player1.player.state.isD()) {
                        if (vel1.x > 0) {
                            this.player1.getBody().applyLinearImpulse(-player1.getBreakSpeedY(), 0, pos1.x, pos1.y, true);
                        } else if (vel1.x < 0) {
                            this.player1.getBody().applyLinearImpulse(player1.getBreakSpeedY(), 0, pos1.x, pos1.y, true);
                        }
                    }

                    if (player1.player.state.isS() && vel1.y > -player1.getMaxVelocityY()) {
                        this.player1.getBody().applyLinearImpulse(0, -player1.getSpeedY(), pos1.x, pos1.y, true);
                    }

                    if (player1.player.state.isW() && vel1.y < player1.getMaxVelocityY()) {
                        this.player1.getBody().applyLinearImpulse(0, player1.getSpeedY(), pos1.x, pos1.y, true);
                    }
                }

                //player 2 networking
                if (player2.player != null) {
                    if (player2.player.state.isA() && vel1.x > -player2.getMaxVelocityX()) {
                        this.player2.getBody().applyLinearImpulse(-player2.getSpeedX(), 0, pos1.x, pos1.y, true);
                        player2.setHeadingLeft(true);
                    }

                    if (player2.player.state.isD() && vel1.x < player2.getMaxVelocityX()) {
                        this.player2.getBody().applyLinearImpulse(player2.getSpeedX(), 0, pos1.x, pos1.y, true);
                        player2.setHeadingLeft(false);
                    }

                    if (!player2.player.state.isA() && !player2.player.state.isD()) {
                        if (vel1.x > 0) {
                            this.player2.getBody().applyLinearImpulse(-player2.getBreakSpeedY(), 0, pos1.x, pos1.y, true);
                        } else if (vel1.x < 0) {
                            this.player2.getBody().applyLinearImpulse(player2.getBreakSpeedY(), 0, pos1.x, pos1.y, true);
                        }
                    }

                    if (player2.player.state.isS() && vel1.y > -player2.getMaxVelocityY()) {
                        this.player2.getBody().applyLinearImpulse(0, -player2.getSpeedY(), pos1.x, pos1.y, true);
                    }

                    if (player2.player.state.isW() && vel1.y < player2.getMaxVelocityY()) {
                        this.player2.getBody().applyLinearImpulse(0, player2.getSpeedY(), pos1.x, pos1.y, true);
                    }
                }

                world.step(1 / 45f, 6, 2);
                //update player states
                if (player1.player != null) {
                    player1.player.updateState(player1.getBody().getPosition().x,
                            player1.getBody().getPosition().y, player1.getBody().getLinearVelocity().x,
                            player1.getBody().getLinearVelocity().y, player2.getBody().getPosition().x,
                            player2.getBody().getPosition().x, player2.getBody().getLinearVelocity().x,
                            player2.getBody().getLinearVelocity().y);
                            //System.out.println("p1 state sent");
                }
                if (player2.player != null) {
                    player2.player.updateState(player1.getBody().getPosition().x,
                            player1.getBody().getPosition().y, player1.getBody().getLinearVelocity().x,
                            player1.getBody().getLinearVelocity().y, player2.getBody().getPosition().x,
                            player2.getBody().getPosition().x, player2.getBody().getLinearVelocity().x,
                            player2.getBody().getLinearVelocity().y);
                            //System.out.println("p2 state sent");

                }
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
                    player.setPort(packet.getPort());
                    //buffer = packet.getData();
                    wrapped = ByteBuffer.wrap(packet.getData());
                    wrapped.position(0);
                    if (wrapped.getInt() == protocolID) {
                        wrapped.position(4);
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
                                /*bitIndex = 31 - (player.ack - remoteSequence); //get index of bit to be set
                                if (!player.localAckSet[bitIndex]) {
                                    player.localAckSet[bitIndex] = true;
                                }*/
                            }
                            wrapped.position(8);
                            remoteAck = wrapped.getInt(); //get remote ack

                            wrapped.position(12);
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
                        /*System.out.println("Local Sequence: " + player.sequence);
                        System.out.println("Remote Sequence: " + player.ack);
                        for (int i = player.remoteAckSet.length - 1; i >= 0; i--) {
                            System.out.println(i + " " + player.remoteAckSet[i]);
                        }*/

                        //resend lost packets
                        /*long now = Instant.now().getEpochSecond();
                        for (PacketContainer pc : player.outPackets) {
                            ByteBuffer newWrapped = ByteBuffer.wrap(pc.getPacket().getData());
                            newWrapped.position(4);
                            int sequenceNumber = newWrapped.getInt();
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
                        }*/
                        wrapped.position(16);
                        if (wrapped.getInt() == 1) { //code 1 is for input updates
                            wrapped.position(20);
                            if (wrapped.getInt() == 1) {
                                player.state.setW(true);
                            } else {
                                player.state.setW(false);
                            }

                            wrapped.position(24);
                            if (wrapped.getInt() == 1) {
                                player.state.setA(true);
                            } else {
                                player.state.setA(false);
                            }

                            wrapped.position(28);
                            if (wrapped.getInt() == 1) {
                                player.state.setS(true);
                            } else {
                                player.state.setS(false);
                            }

                            wrapped.position(32);
                            if (wrapped.getInt() == 1) {
                                player.state.setD(true);
                            } else {
                                player.state.setD(false);
                            }
                            
                            //System.out.println("W: " + player.state.isW() + " A: " + player.state.isA() + " S: " + player.state.isS() + " D: " + player.state.isD());
                        }
                        
                        //System.out.println(packet.getAddress().toString());
                        //System.out.println(packet.getPort());

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
                    byte[] out = new byte[32];
                    ByteBuffer wrapped = ByteBuffer.wrap(out);
                    wrapped.position(0);
                    // accepted connection
                    //int is 0 for p1 and 1 for p2
                    if (player1.player == null) {
                        player1.player = newPlayer;
                        wrapped.putInt(0);
                    } else {
                        player2.player = newPlayer;
                        wrapped.putInt(1);
                    }

                    wrapped.position(4);
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

    public synchronized void submitInput(InputState input) {

    }

    public class ListenerClass implements ContactListener {

        @Override
        public void endContact(Contact contact) {
            ArrayList<String> names = new ArrayList<String>();
            names.add(((BodyData) contact.getFixtureA().getBody().getUserData()).getName());
            names.add(((BodyData) contact.getFixtureB().getBody().getUserData()).getName());
            if ((names.contains("player1") || names.contains("player2")) && names.contains("ground")) {
                if (names.contains("player1")) {
                    player1.setOnGround(false);
                } else if (names.contains("player2")) {
                    player2.setOnGround(false);
                }
            }
        }

        @Override
        public void beginContact(Contact contact) {
            ArrayList<String> names = new ArrayList<String>();
            BodyData bodyDataA = (BodyData) contact.getFixtureA().getBody().getUserData();
            BodyData bodyDataB = (BodyData) contact.getFixtureB().getBody().getUserData();
            names.add(bodyDataA.getName());
            names.add(bodyDataB.getName());
            if ((names.contains("player1") || names.contains("player2")) && names.contains("ground")) {
                if (names.contains("player1")) {
                    player1.setOnGround(true);
                } else if (names.contains("player2")) {
                    player2.setOnGround(true);
                }
            }
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {

        }

    }
}
