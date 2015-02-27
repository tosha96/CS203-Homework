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
    private int gridSize = 1000;
    public Cell[][] matrix = new Cell[gridSize][gridSize];
    
    public void setUpGrid() {
        for (int i=0; i<this.gridSize; i++) {
            for (int j=0; j<this.gridSize; j++) {
                this.matrix[i][j] = new Cell();
            }
        }
        
        matrix[503][502].setAlive(true);
        matrix[503][503].setAlive(true);
        matrix[503][504].setAlive(true);
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
                int aliveCount = 0; //count of alive neighbors
                
                for (int k=-1;k<=1;k++) {
                    for (int m=-1;m<=1;m++) {
                        if (k == 0 && m == 0) { //make sure that we don't check relationship to self
                            continue;
                        }
                        if ((i+k) < 0 || (j+m) < 0 || (i+k) >= this.gridSize || (j+m) >= this.gridSize) { //treat cells outside bounds as dead cells
                            continue;
                        }
                        if (matrix[i+k][j+m].isAlive() == true) {
                            aliveCount++;
                        }
                    }
                }
                
                if (matrix[i][j].isAlive() == true) { //logic to control cell birth and death
                    if (aliveCount < 2) {
                        matrix[i][j].setAliveNext(false);
                    } else if (aliveCount == 2 || aliveCount == 3) {
                        matrix[i][j].setAliveNext(true);
                    } else if (aliveCount > 3) {
                        matrix[i][j].setAliveNext(false);
                    }
                } else {
                    if (aliveCount == 3) {
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
