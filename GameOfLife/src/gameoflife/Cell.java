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
