package com.tosha.fighterserver;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.net.*;

public class FighterServer {
    static final int protocolID = 68742731;
    
    public static void main(String[] arg) {
        FighterServer server = new FighterServer();
        server.go();
    }

    public void go() {
        try {
            DatagramSocket socket = new DatagramSocket(4445);
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            
            while (true) {
                socket.receive(packet);
                buffer = packet.getData();
                
                System.out.println(new String(buffer, 0, buffer.length));
                
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buffer, buffer.length, address, port);
                socket.send(packet);
            }
        } catch (Exception ex) {}
    }
}
