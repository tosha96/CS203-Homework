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
public class Game {

    private GameHelper helper = new GameHelper();
    public Player p1 = new Player();
    public Player p2 = new Player();
    private boolean running = true;

    public void startGame() {

        p1.setSetUpFile("player1.txt");
        p2.setSetUpFile("player2.txt");

        p1.setName("Player 1");
        p2.setName("Player 2");

        p1.setUpPlayer();
        p1.setUpPlayer();

        System.out.println("Welome to battleship!");
    }

    public static void drawBoard(Board board) {
        System.out.println(" 12345");
        for (int i = 0; i < board.getBoardSize(); i++) { //print game display
            String output = "" + (i + 1); //add 1 to row numbers for readability
            for (int j = 0; j < board.getBoardSize(); j++) {
                if (board.matrix[i][j].getIsHidden() == true) {
                    output = output + "|"; // not a hit or a miss
                } else {
                    if (board.matrix[i][j].getIsShip() == false) {
                        output = output + "X"; // previous miss
                    } else if (board.matrix[i][j].getIsShip() == true) {
                        output = output + "O"; // previous hit
                    } else {
                        System.out.println("Unrecognized cell state!");
                    }
                }

            }
            System.out.println(output);
        }
    }

    public int[] getRowColumn() {
        int[] result = new int[2];
        result[0] = Integer.parseInt(helper.GetUserInput("Enter row number:"));
        result[1] = Integer.parseInt(helper.GetUserInput("Enter column number:"));

        return result;
    }

    public void takeTurn(Player player) {
        System.out.println(player.getName() + "'s turn:");
        drawBoard(player.board);
        int[] guess = getRowColumn();
        int result = player.tryHit(guess[0], guess[1]);

        if (result == 0) {
            System.out.println("Miss!");
        } else if (result == 1) {
            System.out.println("Hit!");
        } else if (result == 2) {
            System.out.println("You already tried this square!");
        }
    }

    public void update() {
        if (p2.getHits() >= p1.getLife()) {
            System.out.println("Player 2 wins!");
            return;
        }
        //player 1 goes first
        takeTurn(p1);
        
        if (p1.getHits() >= p2.getLife()) {
            System.out.println("Player 1 wins!");
            return;
        }        
        
        takeTurn(p2);
    }

}
