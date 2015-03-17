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
import java.util.ArrayList;
import java.util.BitSet;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Fighter extends ApplicationAdapter {

    Texture img;
    Box2DDebugRenderer debugRenderer;
    World world;
    OrthographicCamera camera;
    Body groundBody;
    Player player;

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
    InetAddress address;
    byte[] buffer = new byte[256];
    DatagramPacket packet;
    ByteBuffer wrapped;
    int sequence;
    int ack;
    BitSet localAckSet = new BitSet(32);
    BitSet remoteAckSet = new BitSet(32);
    ArrayBlockingQueue<DatagramPacket> inPackets = new ArrayBlockingQueue<DatagramPacket>(33);
    ArrayBlockingQueue<DatagramPacket> outPackets = new ArrayBlockingQueue<DatagramPacket>(33);

    static final int protocolID = 68742731;

    @Override
    public void create() {
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName("127.0.0.1");
            Thread readerThread = new Thread(new IncomingReader());
            readerThread.start();
        } catch (Exception ex) {
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

        Box2D.init();
        world = new World(new Vector2(0, -20), true);
        world.setContactListener(new ListenerClass());
        debugRenderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        player = new Player(world);

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

    }

    @Override
    public void render() {
        stateTime += Gdx.graphics.getDeltaTime();

        Vector2 vel = player.getBody().getLinearVelocity();
        Vector2 pos = player.getBody().getPosition();

        //WASD to move
        if (Gdx.input.isKeyPressed(Keys.A) && vel.x > -player.getMaxVelocityX()) {
            this.player.getBody().applyLinearImpulse(-player.getSpeedX(), 0, pos.x, pos.y, true);
            player.setHeadingLeft(true);
            //currentFrame.flip(true, false);
        }

        if (Gdx.input.isKeyPressed(Keys.D) && vel.x < player.getMaxVelocityX()) {
            this.player.getBody().applyLinearImpulse(player.getSpeedX(), 0, pos.x, pos.y, true);
            player.setHeadingLeft(false);
            //currentFrame.flip(false, false);
        }

        if (!Gdx.input.isKeyPressed(Keys.A) && !Gdx.input.isKeyPressed(Keys.D)) {
            if (vel.x > 0) {
                this.player.getBody().applyLinearImpulse(-player.getBreakSpeedY(), 0, pos.x, pos.y, true);
            } else if (vel.x < 0) {
                this.player.getBody().applyLinearImpulse(player.getBreakSpeedY(), 0, pos.x, pos.y, true);
            }
        }

        if (Gdx.input.isKeyPressed(Keys.S) && vel.y > -player.getMaxVelocityY()) {
            this.player.getBody().applyLinearImpulse(0, -player.getSpeedY(), pos.x, pos.y, true);
        }

        if (Gdx.input.isKeyPressed(Keys.W) && vel.y < player.getMaxVelocityY()) {
            this.player.getBody().applyLinearImpulse(0, player.getSpeedY(), pos.x, pos.y, true);
        }

        if (Gdx.input.isKeyPressed(Keys.O)) {
            try {
                wrapped = ByteBuffer.wrap(buffer);

                wrapped.putInt(protocolID);
                wrapped.position(wrapped.position() + 4);
                wrapped.putInt(sequence);
                wrapped.position(wrapped.position() + 4);
                wrapped.putInt(ack);
                wrapped.position(wrapped.position() + 4);
                wrapped.put(getLocalAckSet().toByteArray());
                wrapped.position(wrapped.position() + 4);

                byte[] echo = "echo".getBytes();
                wrapped.put(echo);

                packet = new DatagramPacket(buffer, buffer.length, address, 4445);
                //outPackets.add(packet); //need to fix like inpackets
                socket.send(packet);
                sequence += 1;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

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
                bd.setX(b.getPosition().x - 30);
                bd.setY(b.getPosition().y - 30);
                if (bd.getName().equals("player")) {
                    if (player.isOnGround()) {
                        if (b.getLinearVelocity().x > 5 || b.getLinearVelocity().x < -5) {
                            currentFrame = walkAnimation.getKeyFrame(stateTime, true);  // #16

                            spriteBatch.begin();
                            //spriteBatch.draw(currentFrame, bd.getX(), bd.getY());      
                            spriteBatch.draw(currentFrame.getTexture(), bd.getX(), bd.getY(), 64, 64, currentFrame.getRegionX(), currentFrame.getRegionY(), currentFrame.getRegionWidth(), currentFrame.getRegionHeight(), player.isHeadingLeft(), false);// #17
                            spriteBatch.end();
                        } else {
                            currentFrame = idleAnimation.getKeyFrame(stateTime, true);  // #16
                            spriteBatch.begin();
                            spriteBatch.draw(currentFrame.getTexture(), bd.getX(), bd.getY(), 64, 64, currentFrame.getRegionX(), currentFrame.getRegionY(), currentFrame.getRegionWidth(), currentFrame.getRegionHeight(), player.isHeadingLeft(), false);             // #17
                            spriteBatch.end();
                        }
                    } else {
                        currentFrame = jumpAnimation.getKeyFrame(stateTime, true);  // #16
                        spriteBatch.begin();
                        spriteBatch.draw(currentFrame.getTexture(), bd.getX(), bd.getY(), 64, 64, currentFrame.getRegionX(), currentFrame.getRegionY(), currentFrame.getRegionWidth(), currentFrame.getRegionHeight(), player.isHeadingLeft(), false);           // #17
                        spriteBatch.end();
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
            if (names.contains("player") && names.contains("ground")) {
                player.setOnGround(false);
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
            if (names.contains("player") && names.contains("ground")) {
                player.setOnGround(true);
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
        DatagramPacket packet;
        ByteBuffer wrapped;
        byte[] remoteAckBytes = new byte[32];
        int remoteSequence; // the server's remote sequence int that we recieve
        int sequenceDifference; // the difference between the remote and local sequence
        int bitIndex; // the index in the bitfield for the packet we are going to set
        int remoteAck;
        BitSet tmpField = new BitSet(32);

        @Override
        public void run() {
            try {
                while (true) {
                    packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    wrapped = ByteBuffer.wrap(packet.getData());
                    if (wrapped.getInt(0) == protocolID) {
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
                                    getLocalAckSet().clear();
                                    ack = remoteSequence;
                                } else {
                                    tmpField.or(getLocalAckSet()); //copy localAckSet
                                    getLocalAckSet().clear();
                                    for (int i = sequenceDifference; i < 32 - sequenceDifference; i++) {
                                        //i - sequenceDifference = the start of the bitset
                                        getLocalAckSet().set(i - sequenceDifference, tmpField.get(i));
                                    }
                                    getLocalAckSet().set(32 - sequenceDifference, true); // push value for last local ack into bitset
                                    ack = remoteSequence; //update local ack if remote sequence is more recent
                                }
                            } else {
                                bitIndex = 32 - (ack - remoteSequence); //get index of bit to be set
                                if (!getLocalAckSet().get(bitIndex)) {
                                    getLocalAckSet().set(bitIndex, true);
                                }
                            }
                            wrapped.position(wrapped.position() + 4);
                            remoteAck = wrapped.getInt(); //get remote ack
                            
                            wrapped.position(wrapped.position() + 4);
                            wrapped.get(remoteAckBytes); //get remote ack bitset
                            for (int i = 0; i < remoteAckBytes.length; i++) {
                                for (int j = 0; j <= 7; j++) {
                                    int bitSetIndex = (i * 7) + i + j; //generates numbers 0-22 for setting bitset values
                                    if (bitSetIndex == 23) {
                                        break;
                                    }
                                    getRemoteAckSet().set(bitSetIndex, isBitSet(remoteAckBytes[i], j));
                                }
                            }
                        }
                        //String received = new String(inBytes, 0, inBytes.length);
                        System.out.println("Local Sequence: " + sequence);
                        System.out.println("Remote Sequence: " + ack);
                        //Gdx.app.log("UDP Message", received);
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
    public synchronized BitSet getLocalAckSet() {
        return localAckSet;
    }

    public synchronized void setLocalAckSet(BitSet localAckSet) {
        this.localAckSet = localAckSet;
    }

    public synchronized BitSet getRemoteAckSet() {
        return remoteAckSet;
    }

    public synchronized void setRemoteAckSet(BitSet remoteAckSet) {
        this.remoteAckSet = remoteAckSet;
    }

}

//https://github.com/libgdx/libgdx/wiki/Box2d
//http://www.gabrielgambetta.com/fpm3.html
