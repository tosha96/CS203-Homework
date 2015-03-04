/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameoflife;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Aantokhin
 */
public class GameOfLife implements ActionListener, ChangeListener {

    /**
     * @param args the command line arguments
     */
    Grid grid = new Grid();
    DrawGrid drawGrid = new DrawGrid();
    ColorSelector colorSelector = new ColorSelector();
    
    JButton pauseButton;
    JButton gridButton;
    JPanel panel = new JPanel();
    JPanel csPanel = new JPanel();
    JPanel sliderPanel = new JPanel();
    JLabel sliderLabel = new JLabel("Speed in MS:");
    
    boolean running = false;
    boolean gridOn = false;
    int horizontalOffset = 500; //offset so cells don't hit wall close to window edges
    int verticalOffset = 500;
    //offset is in unit of cells, must multipy by cell width or length (10) to convert to pixels
    int zoom = 10;
    int speed = 250;
    Color selectedColor = Color.green;

    final static int minSpeed = 0;
    final static int maxSpeed = 500;
    final static int initSpeed = 250;

    JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, minSpeed, maxSpeed, initSpeed);

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
        
        drawGrid.addMouseListener(new MouseAdapter() { //code to get mouse position
            public void mouseClicked(MouseEvent e) {
                Point point = e.getPoint();
                int iCoord = (int) (point.y / zoom) + verticalOffset; 
                //add back in offset to account for where we took it off during drawing
                int jCoord = (int) (point.x / zoom) + horizontalOffset;
                if (running == false) {
                    if (grid.matrix[iCoord][jCoord].isAlive() == true) {
                        grid.matrix[iCoord][jCoord].setAliveNext(false);
                        grid.matrix[iCoord][jCoord].setAlive(false);
                    } else {
                        grid.matrix[iCoord][jCoord].setColor(selectedColor);
                        grid.matrix[iCoord][jCoord].setAliveNext(true);
                        grid.matrix[iCoord][jCoord].setAlive(true);
                    }
                    drawGrid.repaint();
                }
            }
        });
        
        colorSelector.addMouseListener(new MouseAdapter() { //code to get mouse position
            public void mouseClicked(MouseEvent e) {
                Point point = e.getPoint();
                if (point.y < 15 ) {
                    if (point.x < 15) {
                        selectedColor = Color.blue;
                    } else {
                        selectedColor = Color.red;
                    } 
                } else {
                    if (point.x < 15) {
                        selectedColor = Color.black;
                    } else {
                        selectedColor = Color.green;
                    }
                }
            }
        });

        drawGrid.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
        drawGrid.getActionMap().put("left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                horizontalOffset--; //directions are reversed for panning
                drawGrid.repaint();
            }
        });
        drawGrid.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
        drawGrid.getActionMap().put("right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                horizontalOffset++;
                drawGrid.repaint();
            }
        });
        drawGrid.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
        drawGrid.getActionMap().put("up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verticalOffset--;
                drawGrid.repaint();
            }
        });
        drawGrid.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
        drawGrid.getActionMap().put("down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verticalOffset++;
                drawGrid.repaint();
            }
        });
        drawGrid.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0), "plus");
        drawGrid.getActionMap().put("plus", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoom++;
                verticalOffset -= 1 / zoom;
                horizontalOffset -= 1 / zoom;
                drawGrid.repaint();
            }
        });
        drawGrid.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "minus");
        drawGrid.getActionMap().put("minus", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoom--;
                verticalOffset += 1 / zoom;
                horizontalOffset += 1 / zoom;
                drawGrid.repaint();
            }
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pauseButton = new JButton("Unpause"); //starts off paused
        pauseButton.addActionListener(this);
        gridButton = new JButton("Grid on"); //starts off paused
        gridButton.addActionListener(this);
        
        speedSlider.addChangeListener(this);
        speedSlider.setMajorTickSpacing(250);
        speedSlider.setMajorTickSpacing(100);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);

        //drawGrid.setSize(300, 300);
        //colorSelector.setSize(30, 30);
        csPanel.setLayout(new BoxLayout(csPanel, BoxLayout.Y_AXIS));
        csPanel.setPreferredSize(new Dimension(30,30));
        csPanel.add(colorSelector);
        panel.add(csPanel);
        panel.add(pauseButton);
        panel.add(gridButton);

        //keep slider stuff on it's own panel
        sliderPanel.add(sliderLabel);
        sliderPanel.add(speedSlider);
        panel.add(sliderPanel);
        
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.getContentPane().add(BorderLayout.CENTER, drawGrid);//keep drawgrid center so it doesn't shrink

        frame.setSize(600, 600);
        
        frame.setTitle("Game of Life");

        frame.setVisible(true);
        while (1 == 1) {
            if (running == true) {
                grid.updateGrid();
                drawGrid.repaint();
            }
            Thread.sleep(speed);
        }
    }

    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) {
            if (source == speedSlider) {
                speed = source.getValue();
            }
        }
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == pauseButton) {
            running = !running; //toggle running state
            if (running) {
                pauseButton.setText("Pause");
            } else {
                pauseButton.setText("Unpause");
            }
        }

        if (event.getSource() == gridButton) {
            gridOn = !gridOn; //toggle running state
            if (gridOn) {
                gridButton.setText("Grid off");
            } else {
                gridButton.setText("Grid on");
            }
            drawGrid.repaint();
        }
    }

    class DrawGrid extends JPanel { //moved from lifegrid.java

        public void paintComponent(Graphics g) {
            g.setColor(Color.white);
            g.fillRect(0, 0, this.getWidth(), this.getWidth());
            //g.setColor(Color.green);
            for (int i = 0; i < grid.getGridSize(); i++) {
                int iCoord = (i * zoom) - (verticalOffset * zoom); 
                //subtract an offset so game displays cell matrix[+offset][+offset]
                for (int j = 0; j < grid.getGridSize(); j++) {
                    int jCoord = (j * zoom) - (horizontalOffset * zoom);
                    if (grid.matrix[i][j].isAlive() == true) {
                        Color cellColor = grid.matrix[i][j].getColor();
                        //Color cellColor = new Color(100,100,100);
                        g.setColor(cellColor);
                        g.fillRect(jCoord, iCoord, zoom, zoom); //same setup as battleship coords
                        //j and i coords switched?
                    }
                    if (gridOn) {
                        g.setColor(Color.black);
                        g.fillRect(jCoord, 0, 1, this.getHeight()); //vertical grid lines
                    }
                }
                if (gridOn) {
                    g.setColor(Color.black);
                    g.fillRect(0, iCoord, this.getWidth(), 1); //horizontal grid lines

                }
            }
        }

    }
    
    class ColorSelector extends JPanel {
        public void paintComponent(Graphics g) {
            g.setColor(Color.blue);
            g.fillRect(0, 0, 15, 15);
            g.setColor(Color.red);
            g.fillRect(15, 0, 15, 15);
            g.setColor(Color.green);
            g.fillRect(15, 15, 15, 15);
            g.setColor(Color.black);
            g.fillRect(0, 15, 15, 15);
        }
    }

}
