/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameoflife;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 *
 * @author Aantokhin
 */
public class GameOfLife implements ActionListener {

    /**
     * @param args the command line arguments
     */
    Grid grid = new Grid();
    JButton button;
    boolean running = true;

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

        grid.setUpGrid();

        DrawGrid drawGrid = new DrawGrid();
        button = new JButton("Pause");
        button.addActionListener(this);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getContentPane().add(BorderLayout.CENTER, drawGrid);
        frame.getContentPane().add(BorderLayout.SOUTH, button);
        frame.setSize(300, 300);

        frame.setVisible(true);
        while (1 == 1) {
            if (running == true) {
                grid.updateGrid();
                drawGrid.repaint();
            }
            Thread.sleep(250);
        }
    }

    public void actionPerformed(ActionEvent event) {
        running = !running; //toggle running state
        if (running) {
            button.setText("Pause");
        } else {
            button.setText("Unpause");
        }
    }

    class DrawGrid extends JPanel { //moved from lifegrid.java

        public void paintComponent(Graphics g) {
            g.setColor(Color.white);
            g.fillRect(0, 0, this.getWidth(), this.getWidth());
            g.setColor(Color.green);
            for (int i = 0; i < grid.getGridSize(); i++) {
                for (int j = 0; j < grid.getGridSize(); j++) {
                    if (grid.matrix[i][j].isAlive() == true) {
                        int iCord = i * 10;
                        int jCord = j * 10;
                        g.fillRect(jCord, iCord, 10, 10); //same setup as battleship coords
                        //j and i coords switched?

                    }
                }
            }
        }

    }

}
