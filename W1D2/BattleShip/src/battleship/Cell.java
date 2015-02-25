/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package battleship;

/**
 *
 * @author Aantokhin
 */
public class Cell {
    private boolean isHidden = true;
    private boolean isShip = false;

    public boolean getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    public boolean getIsShip() {
        return isShip;
    }

    public void setIsShip(boolean isShip) {
        this.isShip = isShip;
    }
    
    
}
