/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameoflife;

import java.util.ArrayList;

/**
 *
 * @author Aantokhin
 */
public class Grid {
    private int gridSize = 1000;
    public Cell[][] matrix = new Cell[gridSize][gridSize];
    
    public void setUpGrid() {
        for (int i=0; i<this.gridSize; i++) {
            for (int j=0; j<this.gridSize; j++) {
                this.matrix[i][j] = new Cell();
            }
        }
        
        matrix[503][502].setAlive(true);
        matrix[503][502].setR(145);
        matrix[503][502].setG(187);
        matrix[503][502].setB(45);
        
        matrix[503][503].setAlive(true);
        matrix[503][503].setR(98);
        matrix[503][503].setG(175);
        matrix[503][503].setB(200);
        
        matrix[503][504].setAlive(true);
        matrix[503][504].setR(120);
        matrix[503][504].setG(21);
        matrix[503][504].setB(190);
        
        matrix[502][504].setAlive(true);
        matrix[501][503].setAlive(true);
        
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }
    
    public void updateGrid() {
        //need to calculate possible neigbors
       /*
        [0]     [0][1][2]
        [1]     [0][1][2]
        [2]     [0][1][2]
        */
        
       /*
        [i-1]     [j-1][j][j+1]
        [i]       [j-1][j][j+1]
        [i+1]     [j-1][j][j+1]
        */
        
        //need a queue for cell births and deaths
        //implemented in aliveNext var
        
        for (int i=0; i<this.gridSize; i++) {
            for (int j=0; j<this.gridSize; j++) { //initial array iteration
                ArrayList<Cell> neighbors = new ArrayList<>(); //count of alive neighbors
                
                for (int k=-1;k<=1;k++) {
                    for (int m=-1;m<=1;m++) {
                        if (k == 0 && m == 0) { //make sure that we don't check relationship to self
                            continue;
                        }
                        if ((i+k) < 0 || (j+m) < 0 || (i+k) >= this.gridSize || (j+m) >= this.gridSize) { //treat cells outside bounds as dead cells
                            continue;
                        }
                        if (matrix[i+k][j+m].isAlive() == true) {
                            neighbors.add(matrix[i+k][j+m]); //add copy of cell to neighbors list if alive
                        }
                    }
                }
                
                if (matrix[i][j].isAlive() == true) { //logic to control cell birth and death
                    if (neighbors.size() < 2) {
                        matrix[i][j].setAliveNext(false);
                    } else if (neighbors.size() == 2 || neighbors.size() == 3) {
                        matrix[i][j].setAliveNext(true);
                    } else if (neighbors.size() > 3) {
                        matrix[i][j].setAliveNext(false);
                    }
                } else {
                    if (neighbors.size() == 3) {
                        int randInt = (int) Math.random() * 3;
                        matrix[i][j].setR(neighbors.get(randInt).getR());
                        matrix[i][j].setG(neighbors.get(randInt).getG());
                        matrix[i][j].setB(neighbors.get(randInt).getB());
                        matrix[i][j].setAliveNext(true);
                    }
                }
            }
        }
        
        for (int i=0; i<this.gridSize; i++) { //now that changes have been calculated, update cells
            for (int j=0; j<this.gridSize; j++) { 
                this.matrix[i][j].update();
            }
        }
    }
    
    
}
