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
public class Launcher {
    
    public static void main(String[] args) {
        boolean isAlive = true;
        BattleShip game = new BattleShip();
        GameHelper helper = new GameHelper();
        
        game.startGame();
        
        while(game.getIsAlive() == true) {
            System.out.println(" 12345");
            for (int i=0; i < game.getMatrixSize(); i++) { //print game display
                String output = "" + (i + 1); //add 1 to row numbers for readability
                for (int j=0; j < game.getMatrixSize(); j++) {
                    if (game.getCellState(i, j) == 0) {
                        output = output + "|"; // not a hit or a miss
                    } else if (game.getCellState(i, j) == 1) {
                        output = output + "X"; // previous miss
                    } else if (game.getCellState(i, j) == 2) {
                        output = output + "O"; // previous hit
                    }
                
                }
                System.out.println(output);
            }
            
            //parse user input for 
            int x = Integer.parseInt(helper.GetUserInput("Enter row number:"));
            int y = Integer.parseInt(helper.GetUserInput("Enter column number:"));
            int result = game.tryHit(x - 1, y - 1); //subtract 1 from row and column numbers to account for readability
            
            if (result == 1) System.out.println("Hit!");
            else if (result == 0) System.out.println("Miss!");
            else if (result == 2) System.out.println("You already tried this square!");
            
            game.update(); //update game to check if we've won
        }
        
        System.out.println("Congratulations, you win!");
    }
    
}
