/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameoflife;

import javax.swing.*;
import java.awt.event.*;
/**
 *
 * @author Aantokhin
 */
public class GameOfLife {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GameOfLife game = new GameOfLife();
        try {
            game.go();
        } catch (InterruptedException e) {
            System.out.println("InterruptedException!");
        }

    }
    
    public void go() throws InterruptedException {
        JFrame frame = new JFrame();
        //JButton button = new JButton("click me");
        Grid grid = new Grid();
        grid.setUpGrid();
        
        LifeGrid drawGrid = new LifeGrid(grid);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.getContentPane().add(drawGrid);
        frame.setSize(300,300);
        
        frame.setVisible(true);
        
        while (true==true) {
            Thread.sleep(2000);
            
        }
    }
    
}
