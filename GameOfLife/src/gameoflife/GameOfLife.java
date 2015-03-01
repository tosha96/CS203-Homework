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
    boolean running = false;
    int offset = 500; //offset so cells don't hit wall close to window edges
    //offset is in unit of cells, must multipy by cell width or length (10) to convert to pixels

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
        button = new JButton("Unpause"); //starts off paused
        button.addActionListener(this);

        drawGrid.addMouseListener(new MouseAdapter() { //code to get mouse position
            public void mouseClicked(MouseEvent e) {
                Point point = e.getPoint();
                int iCoord = (int) (point.y/10) + offset; //add back in offset to account for where we took it off during drawing
                int jCoord = (int) (point.x/10) + offset;
                if (running == false) {
                    if (grid.matrix[iCoord][jCoord].isAlive() == true) {
                        grid.matrix[iCoord][jCoord].setAliveNext(false);
                        grid.matrix[iCoord][jCoord].setAlive(false);
                    } else {
                        grid.matrix[iCoord][jCoord].setAliveNext(true);
                        grid.matrix[iCoord][jCoord].setAlive(true);
                    }
                    drawGrid.repaint();
                }
            }
        });
        
        frame.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {

            }
 
            public void keyTyped(KeyEvent e) {
                // TODO: Do something for the keyTyped event
            }
 
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (e.getKeyCode()== KeyEvent.VK_LEFT)
                {
                    offset ++;
                    drawGrid.repaint();
                }

                else if (e.getKeyCode()== KeyEvent.VK_RIGHT)
                {
                    offset --;
                    drawGrid.repaint();
                }
            }
        });

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
                        int iCoord = (i * 10) - (offset * 10); //subtract an offset so game displays cell matrix[+offset][+offset]
                        int jCoord = (j * 10) - (offset * 10);
                        g.fillRect(jCoord, iCoord, 10, 10); //same setup as battleship coords
                        //j and i coords switched?

                    }
                }
            }
        }

    }

}