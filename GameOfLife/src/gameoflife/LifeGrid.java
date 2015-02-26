/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameoflife;

import javax.swing.*;
import java.awt.*;
/**
 *
 * @author Aantokhin
 */
public class LifeGrid extends JPanel{
    Grid grid = new Grid();
    
    public LifeGrid(Grid grid) {
        this.grid = grid;
    }
    public void paintComponent(Graphics g) {
        g.setColor(Color.green);
        for (int i=0; i<grid.getGridSize(); i++) {
            for (int j=0; j<grid.getGridSize(); j++) {
                if (grid.matrix[i][j].isAlive() == true) {
                    int iCord = i*10;
                    int jCord = j*10;
                    g.fillRect(jCord, iCord, 10, 10); //same setup as battleship coords
                    //j and i coords switched?
                
                }
            }
        }
    }
    
}
