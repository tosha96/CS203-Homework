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
        
        while(game.getIsAlive()) {
            for (int i=0; i < game.getMatrixSize(); i++) { //print game display
                String output = "";
                for (int j=0; j < game.getMatrixSize(); j++) {
                    output = output + game.getGameCellState(i, j);
                }
                System.out.println(output);
            }
            int x = Integer.parseInt(helper.GetUserInput("Your x guess:"));
            int y = Integer.parseInt(helper.GetUserInput("Your y guess:"));
            int result = game.tryHit(x, y);
            if (result == 1) System.out.println("Hit!");
            else if (result == 0) System.out.println("Miss!");
        }
    }
    
}
