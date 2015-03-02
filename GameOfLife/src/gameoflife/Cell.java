/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameoflife;

/**
 *
 * @author Aantokhin
 */
public class Cell {
    private boolean alive = false;
    private boolean aliveNext = false;
    private int r = 100;
    private int g = 100;
    private int b = 100;

    public float getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public float getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public float getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }
    
    public boolean isAliveNext() {
        return aliveNext;
    }

    public void setAliveNext(boolean aliveNext) {
        this.aliveNext = aliveNext;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    
    public void update() {
        this.alive = this.aliveNext;
    }
}
