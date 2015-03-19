/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tosha.fighterserver;

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

    public int getPlayer() {
        return player;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public boolean isW() {
        return w;
    }

    public void setW(boolean w) {
        this.w = w;
    }

    public boolean isA() {
        return a;
    }

    public void setA(boolean a) {
        this.a = a;
    }

    public boolean isS() {
        return s;
    }

    public void setS(boolean s) {
        this.s = s;
    }

    public boolean isD() {
        return d;
    }

    public void setD(boolean d) {
        this.d = d;
    }
    
    
    
}
