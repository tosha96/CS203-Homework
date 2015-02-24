/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package battleship;

/**
 *
 * @author AAntokhin
 */
public class BattleShip {
    private int[][] matrix = new int[5][5]; //matrix holding state of all cells, x and y
    private int[][] userMatrix = new int[5][5]; //matrix holding state of all revealed cells
    private int[][] shipLocations = new int[2][2]; //matrix holding locations for ship cells
    private boolean isAlive;
    
    
    private int getCellState(int x, int y) {
        //1 = no ship, 2 = ship
        return this.matrix[x][y];
    }
    
    public boolean getIsAlive() {
        return isAlive;
    }
    
    public int getGameCellState(int x, int y) {
        if (this.userMatrix[x][y] == 0) {
            return 0;
        } else {
            return this.getCellState(x,y);
        }
    }
    
    private void setCellState(int x, int y, int state) {
        this.matrix[x][y] = state;
    }
        
    public int getMatrixSize() {
        return this.matrix.length;
    }
    
    private void setUpCells() {
        for (int i = 0; i < this.matrix.length; i++) {
            for (int j = 0; j < this.matrix[i].length; j++) {
                this.matrix[i][j] = 1;
                this.userMatrix[i][j] = 0;
                for (int[] ship : this.shipLocations) {
                    if (ship[0] == i && ship[1] == j) { //if coords match, set ship
                        this.matrix[i][j] = 2;
                    } 
                }
            }
        }
    }
    
    public void startGame() {
        this.shipLocations[0][0] = 0;
        this.shipLocations[0][1] = 0;
        
        this.shipLocations[1][0] = 0;
        this.shipLocations[1][1] = 1;
        
        this.isAlive = true;
        setUpCells();
        
    }
    
    public int tryHit(int x, int y) {
        this.userMatrix[x][y] = 1;
        if (this.matrix[x][y] == 2) {
            return 1;
        }
        return 0;
    }
    
    public void update() {
        
    }
}
