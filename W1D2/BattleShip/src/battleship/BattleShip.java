/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package battleship;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author AAntokhin
 */
public class BattleShip {

    private int[][] matrix = new int[5][5]; //matrix holding state of all cells
    private int[][] userMatrix = new int[5][5]; //matrix holding state of all revealed cells
    private int numShipCells;
    private int guesses = 0;
    private boolean isAlive;

    public boolean getIsAlive() {
        return isAlive;
    }

    public int getCellState(int x, int y) {
        if (this.userMatrix[x][y] == 0) {
            return 0;
        } else {
            //1 = no ship, 2 = ship
            return this.matrix[x][y];
        }
    }

    public int getGuesses() {
        return this.guesses;
    }   
    
    private void setCellState(int x, int y, int state) {
        this.matrix[x][y] = state;
    }

    public int getMatrixSize() {
        return this.matrix.length;
    }

    private void setUpCells() {
        Path inFile = Paths.get(System.getProperty("user.dir") + "\\src\\battleship\\gamesetup.txt");

        Charset charset = Charset.forName("US-ASCII");
        try (BufferedReader reader = Files.newBufferedReader(inFile, charset)) {
            String line = null;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                for (int i=0;i<line.length(); i++) {
                    char[] lineArray = line.toCharArray();
                    this.matrix[lineNum][i] = Character.getNumericValue(lineArray[i]);
                    if (Character.getNumericValue(lineArray[i]) == 2) {
                        //in file, 2 = ship, 1 = nothing
                        this.numShipCells++;
                    }
                }
                lineNum++;
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

        for (int i = 0; i < this.userMatrix.length; i++) { //loop to initialize all cells as hidden
            for (int j = 0; j < this.userMatrix[i].length; j++) {
                this.userMatrix[i][j] = 0;
            }
        }
    }

    public void startGame() {
        this.isAlive = true;
        setUpCells();

    }

    public int tryHit(int x, int y) {
        this.guesses++;
        if (this.userMatrix[x][y] == 1) {
            return 2;
        } else {
            this.userMatrix[x][y] = 1;
        }

        if (this.matrix[x][y] == 2) {
            return 1;
        }
        return 0;
    }

    public void update() {
        int hits = 0;
        for (int i = 0; i < this.matrix.length; i++) { //iterate over arrays to check total hits
            for (int j = 0; j < this.matrix[i].length; j++) {
                if (matrix[i][j] == 2 && userMatrix[i][j] == 1) {
                    hits++; //add hit to total
                }
            }
        }

        if (hits >= this.numShipCells) { //if # of hits is greater than # of ship cells, end game
            this.isAlive = false;
        }
    }
}
