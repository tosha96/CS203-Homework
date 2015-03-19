/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tosha.fighter;

/**
 *
 * @author tosha
 */
public class InputState {
    private boolean w;
    private boolean a;
    private boolean s;
    private boolean d;
    private int player;

    public InputState(boolean w, boolean a, boolean s, boolean d, int player) {
        this.player = player;
        this.w = w;
        this.a = a;
        this.s = s;
        this.d = d;
    }
    
    public InputState() {
    }

    public synchronized int getPlayer() {
        return player;
    }

    public synchronized void setPlayer(int player) {
        this.player = player;
    }

    public synchronized boolean isW() {
        return w;
    }

    public synchronized void setW(boolean w) {
        this.w = w;
    }

    public synchronized boolean isA() {
        return a;
    }

    public synchronized void setA(boolean a) {
        this.a = a;
    }

    public synchronized boolean isS() {
        return s;
    }

    public synchronized void setS(boolean s) {
        this.s = s;
    }

    public synchronized boolean isD() {
        return d;
    }

    public synchronized void setD(boolean d) {
        this.d = d;
    }
    
    
    
}
