
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Piotrek
 */
@Path("")
public class CRest {

    private static Board board = new Board();

    private FieldValue myVal = FieldValue.CIRCLE;
    private FieldValue opponentVal = FieldValue.CROSS;

    @GET
    @Path("moves/{x}/{y}")
    public String moveWithState(@PathParam("x") int x, @PathParam("y") int y) {
        board.set(x, y, opponentVal, false);
        int[] myMove = move();
        board.set(myMove[0], myMove[1], myVal, false);
        return board.getHTML();
    }

    @GET
    @Path("move/{x}/{y}")
    public String move(@PathParam("x") int x, @PathParam("y") int y) {
        board.set(x, y, opponentVal, false);
        int[] myMove = move();
        board.set(myMove[0], myMove[1], myVal, false);
        return "" + myMove[0] + " " + myMove[1];
    }
    
    @GET
    @Path("move/reset")
    public String reset() {
        board = new Board();
        return "";
    }

    private String makeMove() {
        return "1 1";
    }

    private int[] move() {
        int[] result = minimax(2, myVal); // depth, max turn
        return new int[]{result[1], result[2]};   // row, col
    }

    private int[] minimax(int depth, FieldValue player) {
        // Generate possible next moves in a List of int[2] of {row, col}.
        List<int[]> nextMoves = generateMoves();

        // mySeed is maximizing; while oppSeed is minimizing
        int bestScore = (player == myVal) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int currentScore;
        int bestRow = -1;
        int bestCol = -1;

        if (nextMoves.isEmpty() || depth == 0) {
            // Gameover or depth reached, evaluate score
            bestScore = evaluate();
        } else {
            for (int[] move : nextMoves) {
                // Try this move for the current "player"
                board.set(move[0], move[1], player, true);
                if (player == myVal) {  // mySeed (computer) is maximizing player
                    currentScore = minimax(depth - 1, opponentVal)[0];
                    if (currentScore > bestScore) {
                        bestScore = currentScore;
                        bestRow = move[0];
                        bestCol = move[1];
                    }
                } else {  // oppSeed is minimizing player
                    currentScore = minimax(depth - 1, myVal)[0];
                    if (currentScore < bestScore) {
                        bestScore = currentScore;
                        bestRow = move[0];
                        bestCol = move[1];
                    }
                }
                // Undo move
                board.set(move[0], move[1], FieldValue.EMPTY, true);
            }
        }
        return new int[]{bestScore, bestRow, bestCol};
    }

    private List<int[]> generateMoves() {
        List<int[]> nextMoves = new ArrayList<int[]>(); // allocate List

        // If gameover, i.e., no next move
        if (hasWon(myVal) || hasWon(opponentVal)) {
            return nextMoves;   // return empty list
        }

        // Search for empty cells and add to the List
        for (int row = board.getMinY() - 1; row <= board.getMaxY() + 1; ++row) {
            for (int col = board.getMinX() - 1; col <= board.getMaxX() + 1; ++col) {
                if (board.get(row, col) == FieldValue.EMPTY) {
                    nextMoves.add(new int[]{row, col});
                }
            }
        }
        return nextMoves;
    }

    private int evaluate() {
        int score = 0;
        int maxX = board.getMaxX();
        int maxY = board.getMaxY();
        if (maxX - 4 < board.getMinX()) {
            maxX = board.getMinX() + 4;
        }
        if (maxY - 4 < board.getMinY()) {
            maxY = board.getMinY() + 4;
        }
        for (int y = board.getMinY(); y <= maxY; y++) {
            for (int x = board.getMinX(); x <= maxX; x++) {
                score += evaluateLine(y, x, y, x + 1, y, x + 2, y, x + 3, y, x + 4);  // row 0
                score += evaluateLine(y + 1, x, y + 1, x + 1, y + 1, x + 2, y + 1, x + 3, y + 1, x + 4);  // row 1
                score += evaluateLine(y + 2, x, y + 2, x + 1, y + 2, x + 2, y + 2, x + 3, y + 2, x + 4);  // row 2
                score += evaluateLine(y + 3, x, y + 3, x + 1, y + 3, x + 2, y + 3, x + 3, y + 3, x + 4);  // row 3
                score += evaluateLine(y + 4, x, y + 4, x + 1, y + 4, x + 2, y + 4, x + 3, y + 4, x + 4);  // row 4
                score += evaluateLine(y, x, y + 1, x, y + 2, x, y + 3, x, y + 4, x);  // col 0
                score += evaluateLine(y, x + 1, y + 1, x + 1, y + 2, x + 1, y + 3, x + 1, y + 4, x + 1);  // col 1
                score += evaluateLine(y, x + 2, y + 1, x + 2, y + 2, x + 2, y + 3, x + 2, y + 4, x + 2);   // col 2
                score += evaluateLine(y, x + 3, y + 1, x + 3, y + 2, x + 3, y + 3, x + 3, y + 4, x + 3);   // col 3
                score += evaluateLine(y, x + 4, y + 1, x + 4, y + 2, x + 4, y + 3, x + 4, y + 4, x + 4);   // col 4
                score += evaluateLine(y, x, y + 1, x + 1, y + 2, x + 2, y + 3, x + 3, y + 4, x + 4);  // diagonal
                score += evaluateLine(y, x + 4, y + 1, x + 3, y + 2, x + 2, y + 3, x + 1, y + 4, x);  // alternate diagonal
            }
        }
        return score;
    }

    /**
     * The heuristic evaluation function for the given line of 3 cells
     *
     * @Return +100, +10, +1 for 3-, 2-, 1-in-a-line for computer. -100, -10, -1
     * for 3-, 2-, 1-in-a-line for opponent. 0 otherwise
     */
    private int evaluateLine(int row1, int col1, int row2, int col2, int row3, int col3, int row4, int col4, int row5, int col5) {
        int score = 0;

        // First cell
        if (board.get(row1, col1) == myVal) {
            score = 1;
        } else if (board.get(row1, col1) == opponentVal) {
            score = -1;
        }

        // Second cell
        if (board.get(row2, col2) == myVal) {
            if (score == 1) {   // cell1 is mySeed
                score = 10;
            } else if (score == -1) {  // cell1 is oppSeed
                return 0;
            } else {  // cell1 is empty
                score = 1;
            }
        } else if (board.get(row2, col2) == opponentVal) {
            if (score == -1) { // cell1 is oppSeed
                score = -10;
            } else if (score == 1) { // cell1 is mySeed
                return 0;
            } else {  // cell1 is empty
                score = -1;
            }
        }

        // Third cell
        if (board.get(row3, col3) == myVal) {
            if (score > 0) {  // cell1 and/or cell2 is mySeed
                score *= 10;
            } else if (score < 0) {  // cell1 and/or cell2 is oppSeed
                return 0;
            } else {  // cell1 and cell2 are empty
                score = 1;
            }
        } else if (board.get(row3, col3) == opponentVal) {
            if (score < 0) {  // cell1 and/or cell2 is oppSeed
                score *= 10;
            } else if (score > 1) {  // cell1 and/or cell2 is mySeed
                return 0;
            } else {  // cell1 and cell2 are empty
                score = -1;
            }
        }

        if (board.get(row4, col4) == myVal) {
            if (score > 0) {  // cell1 and/or cell2 is mySeed
                score *= 10;
            } else if (score < 0) {  // cell1 and/or cell2 is oppSeed
                return 0;
            } else {  // cell1 and cell2 are empty
                score = 1;
            }
        } else if (board.get(row4, col4) == opponentVal) {
            if (score < 0) {  // cell1 and/or cell2 is oppSeed
                score *= 10;
            } else if (score > 1) {  // cell1 and/or cell2 is mySeed
                return 0;
            } else {  // cell1 and cell2 are empty
                score = -1;
            }
        }

        if (board.get(row5, col5) == myVal) {
            if (score > 0) {  // cell1 and/or cell2 is mySeed
                score *= 10;
            } else if (score < 0) {  // cell1 and/or cell2 is oppSeed
                return 0;
            } else {  // cell1 and cell2 are empty
                score = 1;
            }
        } else if (board.get(row5, col5) == opponentVal) {
            if (score < 0) {  // cell1 and/or cell2 is oppSeed
                score *= 10;
            } else if (score > 1) {  // cell1 and/or cell2 is mySeed
                return 0;
            } else {  // cell1 and cell2 are empty
                score = -1;
            }
        }
        return score;
    }

    private boolean hasWon(FieldValue thePlayer) {
        if (board.getHeight() < 5 && board.getWidth() < 5) {
            return false;
        }

        for (int y = board.getMinY(); y <= board.getMaxY(); y++) {
            for (int x = board.getMinX(); x <= board.getMaxX(); x++) {
                return winning(thePlayer, y, x, y, x + 1, y, x + 2, y, x + 3, y, x + 4) ||  // row 0
                winning(thePlayer, y + 1, x, y + 1, x + 1, y + 1, x + 2, y + 1, x + 3, y + 1, x + 4) ||  // row 1
                winning(thePlayer, y + 2, x, y + 2, x + 1, y + 2, x + 2, y + 2, x + 3, y + 2, x + 4) ||  // row 2
                winning(thePlayer, y + 3, x, y + 3, x + 1, y + 3, x + 2, y + 3, x + 3, y + 3, x + 4) ||  // row 3
                winning(thePlayer, y + 4, x, y + 4, x + 1, y + 4, x + 2, y + 4, x + 3, y + 4, x + 4) ||  // row 4
                winning(thePlayer, y, x, y + 1, x, y + 2, x, y + 3, x, y + 4, x) ||  // col 0
                winning(thePlayer, y, x + 1, y + 1, x + 1, y + 2, x + 1, y + 3, x + 1, y + 4, x + 1) ||  // col 1
                winning(thePlayer, y, x + 2, y + 1, x + 2, y + 2, x + 2, y + 3, x + 2, y + 4, x + 2) ||   // col 2
                winning(thePlayer, y, x + 3, y + 1, x + 3, y + 2, x + 3, y + 3, x + 3, y + 4, x + 3) ||   // col 3
                winning(thePlayer, y, x + 4, y + 1, x + 4, y + 2, x + 4, y + 3, x + 4, y + 4, x + 4) ||   // col 4
                winning(thePlayer, y, x, y + 1, x + 1, y + 2, x + 2, y + 3, x + 3, y + 4, x + 4) ||  // diagonal
                winning(thePlayer, y, x + 4, y + 1, x + 3, y + 2, x + 2, y + 3, x + 1, y + 4, x);  // alternate diagonal
            }
        }
        return false;
    }
    
    boolean winning(FieldValue val, int row1, int col1, int row2, int col2, int row3, int col3, int row4, int col4, int row5, int col5) {
        return (board.get(row1, col1) == val &&
                board.get(row2, col2) == val &&
                board.get(row3, col3) == val &&
                board.get(row4, col4) == val &&
                board.get(row5, col5) == val);
    }
}
