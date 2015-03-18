/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tosha.fighter;

import java.net.DatagramPacket;

/**
 *
 * @author Aantokhin
 */
public class PacketContainer {
    private DatagramPacket packet;
    private long timestamp;

    public PacketContainer(DatagramPacket packet, long timestamp) {
        this.packet = packet;
        this.timestamp = timestamp;
    }

    public DatagramPacket getPacket() {
        return packet;
    }

    public void setPacket(DatagramPacket packet) {
        this.packet = packet;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    
    
}
