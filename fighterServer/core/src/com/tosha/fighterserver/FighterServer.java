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

public class FighterServer {

    static final int protocolID = 68742731;
    Vector<Player> players = new Vector<Player>();

    public static void main(String[] arg) {
        FighterServer server = new FighterServer();
        server.go();
    }

    public void go() {
        try {
            DatagramSocket socket = new DatagramSocket(4445);
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            InetAddress address;
            ByteBuffer wrapped;
            int sequence;
            int ack;
            BitSet ackField = new BitSet(32);
            int port;

            while (true) {
                socket.receive(packet);
                buffer = packet.getData();
                wrapped = ByteBuffer.wrap(buffer);
                if (wrapped.getInt(0) == protocolID) {

                    System.out.println(new String(buffer, 0, buffer.length));
                    address = packet.getAddress();
                    port = packet.getPort();
                    packet = new DatagramPacket(buffer, buffer.length, address, port);
                    socket.send(packet);
                }
            }
        } catch (Exception ex) {
        }
    }

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

}
