package com.tosha.fighter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Net.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.net.*;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Fighter extends ApplicationAdapter {

    Texture img;
    Box2DDebugRenderer debugRenderer;
    World world;
    OrthographicCamera camera;
    Body groundBody;
    PlayerEntity player1;
    PlayerEntity player2;

    private static final int FRAME_COLS = 5;
    private static final int FRAME_ROWS = 5;

    Animation idleAnimation;
    Animation walkAnimation;
    Animation jumpAnimation;
    Texture playerSheet;
    TextureRegion[] idleFrames;
    TextureRegion[] walkFrames;
    TextureRegion[] jumpFrames;
    SpriteBatch spriteBatch;
    TextureRegion currentFrame;

    float stateTime;
    Array<Body> bodies = new Array<Body>();

    DatagramSocket socket;
    Socket tcpSocket;
    InetAddress address;
    byte[] buffer = new byte[256];
    DatagramPacket packet;
    ByteBuffer wrapped;
    int sequence;
    int ack;
    boolean[] localAckSet = new boolean[32];
    boolean[] remoteAckSet = new boolean[32];
    ArrayBlockingQueue<PacketContainer> inPackets = new ArrayBlockingQueue<PacketContainer>(33);
    ArrayBlockingQueue<PacketContainer> outPackets = new ArrayBlockingQueue<PacketContainer>(33);
    DataInputStream reader;
    DataOutputStream writer;
    int udpPort;

    static final int protocolID = 68742731;
    InputState state = new InputState();
    PlayerEntity currentPlayer;

    @Override
    public void create() {
        Box2D.init();
        world = new World(new Vector2(0, 0), true);
        //world.setContinuousPhysics(true);
        //world.setContactListener(new ListenerClass());
        debugRenderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        player1 = new PlayerEntity(world, "player1", 100.0f, 100.0f);
        player2 = new PlayerEntity(world, "player2", 400.0f, 100.0f);

        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(new Vector2(0, 10));

        groundBody = world.createBody(groundBodyDef);

        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(camera.viewportWidth, 10.0f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = groundBox;
        fixtureDef.density = 0.0f;
        //fixtureDef.isSensor = true;
        groundBody.createFixture(fixtureDef);
        groundBody.setUserData(new BodyData("ground", groundBody.getPosition().x, groundBody.getPosition().y));
        groundBox.dispose();

        try {
            tcpSocket = new Socket("127.0.0.1", 5001);
            reader = new DataInputStream(tcpSocket.getInputStream());
            writer = new DataOutputStream(tcpSocket.getOutputStream());
            address = InetAddress.getByName("127.0.0.1");
            byte[] inBuffer = new byte[32];
            ByteBuffer inWrapped = ByteBuffer.wrap(inBuffer);
            /*while (reader.read(inBuffer) <= 8) {
             //reader.readFully(inBuffer); //read response w/ UDP port from server
             //just read once
             }*/
            reader.read(inBuffer);
            inWrapped.position(0);
            int connectionSuccess = inWrapped.getInt();
            System.out.println("Connected to server, result: " + connectionSuccess);
            System.out.println("started reader setup");
            inWrapped.position(4);
            udpPort = inWrapped.getInt();
            System.out.println(udpPort);
            socket = new DatagramSocket();
            Thread readerThread = new Thread(new IncomingReader());
            readerThread.start();
            if (connectionSuccess == 0) {
                currentPlayer = player1;
                System.out.println("I am player 1");
            } else if (connectionSuccess == 1) {
                currentPlayer = player2;
                System.out.println("I am player 2");
            }
            System.out.println("reader started");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        playerSheet = new Texture(Gdx.files.internal("fighterspritesheet.png"));
        TextureRegion[][] tmp = TextureRegion.split(playerSheet, playerSheet.getWidth() / FRAME_COLS, playerSheet.getHeight() / FRAME_ROWS);              // #10
        walkFrames = new TextureRegion[3];
        idleFrames = new TextureRegion[2];
        jumpFrames = new TextureRegion[1];

        walkFrames[0] = tmp[1][0];
        walkFrames[1] = tmp[1][1];
        walkFrames[2] = tmp[1][2];

        idleFrames[0] = tmp[0][0];
        idleFrames[1] = tmp[0][1];

        jumpFrames[0] = tmp[0][2];

        walkAnimation = new Animation(0.25f, walkFrames);
        idleAnimation = new Animation(0.25f, idleFrames);
        jumpAnimation = new Animation(0.25f, jumpFrames);
        spriteBatch = new SpriteBatch();
        stateTime = 0f;

    }

    @Override
    public void render() {
        stateTime += Gdx.graphics.getDeltaTime();

        Vector2 vel = currentPlayer.getBody().getLinearVelocity();
        Vector2 pos = currentPlayer.getBody().getPosition();

        this.state.setW(false);
        this.state.setA(false);
        this.state.setS(false);
        this.state.setD(false);

        //WASD to move
        if (Gdx.input.isKeyPressed(Keys.A) && vel.x > -currentPlayer.getMaxVelocityX()) {
            //this.currentPlayer.getBody().applyLinearImpulse(-currentPlayer.getSpeedX(), 0, pos.x, pos.y, true);
            //currentPlayer.setHeadingLeft(true);
            this.state.setA(true);
            //currentFrame.flip(true, false);
        }

        if (Gdx.input.isKeyPressed(Keys.D) && vel.x < currentPlayer.getMaxVelocityX()) {
            //this.currentPlayer.getBody().applyLinearImpulse(currentPlayer.getSpeedX(), 0, pos.x, pos.y, true);
            //currentPlayer.setHeadingLeft(false);
            this.state.setD(true);
            //currentFrame.flip(false, false);
        }

        if (!Gdx.input.isKeyPressed(Keys.A) && !Gdx.input.isKeyPressed(Keys.D)) {
            if (vel.x > 0) {
                // this.currentPlayer.getBody().applyLinearImpulse(-currentPlayer.getBreakSpeedY(), 0, pos.x, pos.y, true);
            } else if (vel.x < 0) {
                // this.currentPlayer.getBody().applyLinearImpulse(currentPlayer.getBreakSpeedY(), 0, pos.x, pos.y, true);
            }
        }

        if (Gdx.input.isKeyPressed(Keys.S) && vel.y > -player1.getMaxVelocityY()) {
            //this.currentPlayer.getBody().applyLinearImpulse(0, -currentPlayer.getSpeedY(), pos.x, pos.y, true);
            this.state.setS(true);
        }

        if (Gdx.input.isKeyPressed(Keys.W) && vel.y < player1.getMaxVelocityY()) {
            //this.currentPlayer.getBody().applyLinearImpulse(0, currentPlayer.getSpeedY(), pos.x, pos.y, true);
            this.state.setW(true);
        }

        //send inputs
        try {
            wrapped = ByteBuffer.wrap(buffer);

            wrapped.putInt(protocolID);
            wrapped.position(4);
            wrapped.putInt(sequence);
            wrapped.position(8);
            wrapped.putInt(ack);
            wrapped.position(12);
            BitSet tempBitSet = new BitSet();
            for (int i = 0; i < getLocalAckSet().length; i++) {
                tempBitSet.set(i, getLocalAckSet()[i]); //convert from boolean[] to bitset
            }
            wrapped.put(tempBitSet.toByteArray()); //its okay to write the whole bitset because the bitset > 32 is just zeroes
            wrapped.position(16);

            wrapped.putInt(1);

            wrapped.position(20);
            if (this.state.isW()) {
                wrapped.putInt(1);
            } else {
                wrapped.putInt(0);
            }

            wrapped.position(24);
            if (this.state.isA()) {
                wrapped.putInt(1);
            } else {
                wrapped.putInt(0);
            }

            wrapped.position(28);
            if (this.state.isS()) {
                wrapped.putInt(1);
            } else {
                wrapped.putInt(0);
            }

            wrapped.position(32);
            if (this.state.isD()) {
                wrapped.putInt(1);
            } else {
                wrapped.putInt(0);
            }

            packet = new DatagramPacket(buffer, buffer.length, address, udpPort);
            if (!outPackets.isEmpty()) {
                outPackets.take(); //remove oldest packet from recieved packet queue
            }
            outPackets.add(new PacketContainer(packet, Instant.now().getEpochSecond())); //need to fix like inpackets

            socket.send(packet);
            sequence += 1;
            //System.out.println("input sent");
        } catch (Exception ex) {
        }

        /*if (Gdx.input.isKeyPressed(Keys.O)) {
         try {
         wrapped = ByteBuffer.wrap(buffer);

         wrapped.putInt(protocolID);
         wrapped.position(wrapped.position() + 4);
         wrapped.putInt(sequence);
         wrapped.position(wrapped.position() + 4);
         wrapped.putInt(ack);
         wrapped.position(wrapped.position() + 4);
         BitSet tempBitSet = new BitSet();
         for (int i = 0; i < getLocalAckSet().length; i++) {
         tempBitSet.set(i, getLocalAckSet()[i]); //convert from boolean[] to bitset
         }
         wrapped.put(tempBitSet.toByteArray()); //its okay to write the whole bitset because the bitset > 32 is just zeroes
         wrapped.position(wrapped.position() + 4);

         byte[] echo = "echo".getBytes();
         wrapped.put(echo);

         packet = new DatagramPacket(buffer, buffer.length, address, udpPort);
         if (!outPackets.isEmpty()) {
         outPackets.take(); //remove oldest packet from recieved packet queue
         }
         outPackets.add(new PacketContainer(packet, Instant.now().getEpochSecond())); //need to fix like inpackets

         socket.send(packet);
         sequence += 1;
         } catch (Exception ex) {
         ex.printStackTrace();
         }
         }*/
        world.step(1 / 45f, 6, 2);
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        debugRenderer.render(world, camera.combined);
        world.getBodies(bodies);

        for (Body b : bodies) {

            BodyData bd = (BodyData) b.getUserData();

            if (bd != null) {
                // Update the entities/sprites position and angle
                bd.setX(b.getPosition().x - 31);
                bd.setY(b.getPosition().y - 31);
                if (bd.getName().contains("player")) {
                    if (bd.getName().equals("player1")) {
                        if (player1.isOnGround()) {
                            if (b.getLinearVelocity().x > 5 || b.getLinearVelocity().x < -5) {
                                currentFrame = walkAnimation.getKeyFrame(stateTime, true);  // #16

                                spriteBatch.begin();
                                //spriteBatch.draw(currentFrame, bd.getX(), bd.getY());      
                                spriteBatch.draw(currentFrame.getTexture(), bd.getX(), bd.getY(), 64, 64, currentFrame.getRegionX(), currentFrame.getRegionY(), currentFrame.getRegionWidth(), currentFrame.getRegionHeight(), player1.isHeadingLeft(), false);// #17
                                spriteBatch.end();
                            } else {
                                currentFrame = idleAnimation.getKeyFrame(stateTime, true);  // #16
                                spriteBatch.begin();
                                spriteBatch.draw(currentFrame.getTexture(), bd.getX(), bd.getY(), 64, 64, currentFrame.getRegionX(), currentFrame.getRegionY(), currentFrame.getRegionWidth(), currentFrame.getRegionHeight(), player1.isHeadingLeft(), false);             // #17
                                spriteBatch.end();
                            }
                        } else {
                            currentFrame = jumpAnimation.getKeyFrame(stateTime, true);  // #16
                            spriteBatch.begin();
                            spriteBatch.draw(currentFrame.getTexture(), bd.getX(), bd.getY(), 64, 64, currentFrame.getRegionX(), currentFrame.getRegionY(), currentFrame.getRegionWidth(), currentFrame.getRegionHeight(), player1.isHeadingLeft(), false);           // #17
                            spriteBatch.end();
                        }
                    } else if (bd.getName().equals("player2")) {
                        if (player2.isOnGround()) {
                            if (b.getLinearVelocity().x > 5 || b.getLinearVelocity().x < -5) {
                                currentFrame = walkAnimation.getKeyFrame(stateTime, true);  // #16

                                spriteBatch.begin();
                                //spriteBatch.draw(currentFrame, bd.getX(), bd.getY());      
                                spriteBatch.draw(currentFrame.getTexture(), bd.getX(), bd.getY(), 64, 64, currentFrame.getRegionX(), currentFrame.getRegionY(), currentFrame.getRegionWidth(), currentFrame.getRegionHeight(), player2.isHeadingLeft(), false);// #17
                                spriteBatch.end();
                            } else {
                                currentFrame = idleAnimation.getKeyFrame(stateTime, true);  // #16
                                spriteBatch.begin();
                                spriteBatch.draw(currentFrame.getTexture(), bd.getX(), bd.getY(), 64, 64, currentFrame.getRegionX(), currentFrame.getRegionY(), currentFrame.getRegionWidth(), currentFrame.getRegionHeight(), player2.isHeadingLeft(), false);             // #17
                                spriteBatch.end();
                            }
                        } else {
                            currentFrame = jumpAnimation.getKeyFrame(stateTime, true);  // #16
                            spriteBatch.begin();
                            spriteBatch.draw(currentFrame.getTexture(), bd.getX(), bd.getY(), 64, 64, currentFrame.getRegionX(), currentFrame.getRegionY(), currentFrame.getRegionWidth(), currentFrame.getRegionHeight(), player2.isHeadingLeft(), false);           // #17
                            spriteBatch.end();
                        }
                    }
                }
            }
        }
        //batch.begin();
        //batch.draw(img, 0, 0);
        //batch.end();
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
                Gdx.app.log("MyTag", "In Air");
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
                Gdx.app.log("MyTag", "On Ground");
            }
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };

    public class IncomingReader implements Runnable {

        //packet, wrapped, buffer, etc. are all different from ones initialized at the top of the class
        //these are used for reciving, those are used for sending
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        ByteBuffer wrapped;
        byte[] remoteAckBytes = new byte[4];
        int remoteSequence; // the server's remote sequence int that we recieve
        int sequenceDifference; // the difference between the remote and local sequence
        int bitIndex; // the index in the bitfield for the packet we are going to set
        int remoteAck;
        //int port;
        boolean[] tmpSet = new boolean[32];

        float remoteX = 0;
        float remoteY = 0;
        float remoteVelX = 0;
        float remoteVelY = 0;

        /*public IncomingReader(int port) {
         this.port = port;
         }*/
        @Override
        public void run() {
            try {
                //socket = new DatagramSocket();
                while (true) {
                    System.out.println("loop is running");
                    socket.receive(packet);
                    wrapped = ByteBuffer.wrap(packet.getData());
                    wrapped.position(0);

                    if (wrapped.getInt() == protocolID) {
                        wrapped.position(4);
                        remoteSequence = wrapped.getInt();
                        if (remoteSequence >= ack - 32) { //make sure packet is recent enough to matter
                            if (!inPackets.isEmpty()) {
                                inPackets.take(); //remove oldest packet from recieved packet queue
                            }
                            inPackets.add(new PacketContainer(packet, Instant.now().getEpochSecond())); //add newest packet to queue

                            if (remoteSequence > ack) {
                                sequenceDifference = remoteSequence - ack;
                                if (sequenceDifference > 32) { //clear local acks if they are all too outdated
                                    setLocalAckSet(new boolean[32]);
                                    ack = remoteSequence;
                                } else {
                                    tmpSet = Arrays.copyOfRange(getLocalAckSet(), sequenceDifference, 31); //copy localAckSet
                                    setLocalAckSet(new boolean[32]);
                                    for (int i = 0; i < tmpSet.length; i++) {
                                        getLocalAckSet()[i] = tmpSet[i];
                                    }
                                    /*for (int i = sequenceDifference; i < 32 - sequenceDifference; i++) {
                                     //i - sequenceDifference = the start of the bitset
                                     getLocalAckSet()[i - sequenceDifference] = tmpSet[i];
                                     System.out.println((i - sequenceDifference) + " " + tmpSet[i]);
                                     }*/
                                    getLocalAckSet()[31 - sequenceDifference] = true; // push value for last local ack into bitset
                                    ack = remoteSequence; //update local ack if remote sequence is more recent
                                }
                            } else {
                                /*bitIndex = 31 - (ack - remoteSequence); //get index of bit to be set
                                 if (!getLocalAckSet()[bitIndex]) {
                                 getLocalAckSet()[bitIndex] = true;
                                 }*/
                            }
                            wrapped.position(8);
                            remoteAck = wrapped.getInt(); //get remote ack

                            wrapped.position(12);
                            wrapped.get(remoteAckBytes); //get remote ack bitset

                            for (int i = 0; i < remoteAckBytes.length; i++) {
                                for (int j = 0; j <= 7; j++) {
                                    int bitSetIndex = (i * 7) + i + j; //generates numbers 0-22 for setting bitset values
                                    if (bitSetIndex == 32) {
                                        break;
                                    }
                                    getRemoteAckSet()[bitSetIndex] = isBitSet(remoteAckBytes[i], j);
                                }
                            }
                        }
                        //String received = new String(inBytes, 0, inBytes.length);
                        /*System.out.println("Local Sequence: " + sequence);
                         System.out.println("Remote Sequence: " + ack);
                         for (int i = getRemoteAckSet().length - 1; i >= 0; i--) {
                         System.out.println(i + " " + getRemoteAckSet()[i]);
                         }*/
                        //Gdx.app.log("UDP Message", received);
                    }

                    //resend lost packets
                    /* now = Instant.now().getEpochSecond();
                     for (PacketContainer pc : outPackets) {
                     ByteBuffer newWrapped = ByteBuffer.wrap(pc.getPacket().getData());
                     newWrapped.position(4);
                     int sequenceNumber = newWrapped.getInt();
                     if (sequenceNumber != ack) {
                     //check to see if packed is acked in ackset
                     //resend if it isn't
                     int ackIndex = ack - sequenceNumber;

                     if (ackIndex >= 0 && ackIndex < 32) {
                     if (!getRemoteAckSet()[31 - (ack - sequenceNumber)] && now - pc.getTimestamp() >= 1) {
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
                    System.out.println(wrapped.getInt());
                    wrapped.position(16);
                    if (wrapped.getInt() == 0) { //world state update
                        wrapped.position(20);

                        //player 1
                        while (world.isLocked()) {
                            Thread.sleep(1);
                        }

                        remoteX = wrapped.getFloat();
                        wrapped.position(24);
                        remoteY = wrapped.getFloat();
                        wrapped.position(28);

                        player1.getBody().setTransform(remoteX, remoteY, 0);

                        remoteVelX = wrapped.getFloat();
                        wrapped.position(32);
                        remoteVelY = wrapped.getFloat();
                        wrapped.position(36);

                        player1.getBody().setLinearVelocity(new Vector2(remoteVelX, remoteVelY));

                        //player 2
                        remoteX = wrapped.getFloat();
                        wrapped.position(40);
                        remoteY = wrapped.getFloat();
                        wrapped.position(44);

                        player2.getBody().setTransform(remoteX, remoteY, 0);

                        remoteVelX = wrapped.getFloat();
                        wrapped.position(48);
                        remoteVelY = wrapped.getFloat();

                        player2.getBody().setLinearVelocity(new Vector2(remoteVelX, remoteVelY));

                        wrapped.position(52);
                        if (wrapped.getInt() == 1) {
                            player1.setHeadingLeft(true);
                        } else {
                            player1.setHeadingLeft(false);
                        }

                        wrapped.position(56);
                        if (wrapped.getInt() == 1) {
                            player2.setHeadingLeft(true);
                        } else {
                            player2.setHeadingLeft(false);
                        }

                        wrapped.position(60);
                        if (wrapped.getInt() == 1) {
                            player1.setOnGround(true);
                        } else {
                            player1.setOnGround(false);
                        }

                        wrapped.position(64);
                        if (wrapped.getInt() == 1) {
                            player2.setOnGround(true);
                        } else {
                            player2.setOnGround(false);
                        }

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

    //wrappers to make sure threads deal with bitsets nicely
    public synchronized boolean[] getLocalAckSet() {
        return localAckSet;
    }

    public synchronized void setLocalAckSet(boolean[] localAckSet) {
        this.localAckSet = localAckSet;
    }

    public synchronized boolean[] getRemoteAckSet() {
        return remoteAckSet;
    }

    public synchronized void setRemoteAckSet(boolean[] remoteAckSet) {
        this.remoteAckSet = remoteAckSet;
    }

}

//https://github.com/libgdx/libgdx/wiki/Box2d
//http://www.gabrielgambetta.com/fpm3.html
