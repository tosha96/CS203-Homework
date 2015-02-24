/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package battleship;

import java.io.*;
import java.net.URI;
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
    private int[][] shipLocations = new int[3][2]; //matrix holding locations for ship cells
    private int numShipCells;
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
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

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

        this.shipLocations[2][0] = 0;
        this.shipLocations[2][1] = 2;

        this.isAlive = true;
        setUpCells();

    }

    public int tryHit(int x, int y) {
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

        if (hits >= this.shipLocations.length) { //if # of hits is greater than # of ship cells, end game
            this.isAlive = false;
        }
    }
}
