/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package battleship;

/**
 *
 * @author Aantokhin
 */
public class Player {
    private int guesses;
    private int hits;
    private int life;
    private String setUpFile;
    private String name;
    Board board = new Board();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGuesses() {
        return guesses;
    }

    public void setGuesses(int guesses) {
        this.guesses = guesses;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public String getSetUpFile() {
        return setUpFile;
    }

    public void setSetUpFile(String setUpFile) {
        this.setUpFile = setUpFile;
    }
    
    public void setUpPlayer() {
        this.board.setUpCells(setUpFile);
        this.life = this.board.getNumShipCells();
    }
    
    public int tryHit(int x,int y, Board board) {
        //0 = miss
        //1 = hit
        //2 = already attacked here
        
        this.guesses++;
        
        int result = board.tryHit(x, y);
        if (result == 1) {
            this.hits++;
        }
        
        return result;
    }
}
