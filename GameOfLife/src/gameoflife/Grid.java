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
public class Grid {
    private int gridSize = 10;
    public Cell[][] matrix = new Cell[gridSize][gridSize];
    
    public void setUpGrid() {
        for (int i=0; i<this.gridSize; i++) {
            for (int j=0; j<this.gridSize; j++) {
                this.matrix[i][j] = new Cell();
            }
        }
        matrix[0][0].setAlive(true);
        matrix[1][0].setAlive(true);
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
        [i+1]     [j-1][j][j-1]
        */
        
        //need a que for cell births and deaths
    }
    
    
}
