/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameoflife;

import java.awt.Color;

/**
 *
 * @author Aantokhin
 */
public class Cell {
    private boolean alive = false;
    private boolean aliveNext = false;
    private Color color = Color.green;

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
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
