/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package battleship;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Aantokhin
 */
public class Board {
    private int boardSize = 5;
    private int numShipCells;
    public Cell[][] matrix = new Cell[boardSize][boardSize];

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public int getNumShipCells() {
        return numShipCells;
    }
    
    public void setUpCells(String setUpFile) {
        Path inFile = Paths.get(System.getProperty("user.dir") + "\\src\\battleship\\" + setUpFile);

        Charset charset = Charset.forName("US-ASCII");
        try (BufferedReader reader = Files.newBufferedReader(inFile, charset)) {
            String line = null;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                for (int i=0;i<line.length(); i++) {
                    char[] lineArray = line.toCharArray();
                    this.matrix[lineNum][i] = new Cell();
                    if (lineArray[i] == 'O') {
                        this.numShipCells++;
                        this.matrix[lineNum][i].setIsShip(true);
                    } else if (lineArray[i] == 'X') {
                        this.matrix[lineNum][i].setIsShip(false);
                    } else {
                        System.out.println("Unknown character type in game setup file.s");
                    }
                    //this.matrix[lineNum][i] = Character.getNumericValue(lineArray[i]);

                }
                lineNum++;
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    public int tryHit(int x, int y) {
        if (this.matrix[x][y].getIsHidden() == false) {
            return 2;
        } else {
            this.matrix[x][y].setIsHidden(false);
        }

        if (this.matrix[x][y].getIsShip() == true) {
            return 1;
        }
        return 0;
    }
}
