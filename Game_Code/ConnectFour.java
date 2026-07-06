import java.util.*;

// A class to represent a game of Connect Four that extends the
// AbstractStrategyGame class. The game is played on a 7-column by 6-row grid
// where players take turns dropping tokens into columns or removing their own
// tokens from the bottom. The first player to get four tokens in a row
// (horizontally, vertically, or diagonally) wins.
public class ConnectFour extends AbstractStrategyGame {

    private char[][] board;
    private boolean player1Turn;
    private int winner;

    // Constructs a new Connect Four game with an empty 6x7 board.
    // Player 1 (X) goes first.
    public ConnectFour() {
        int rows = 6;
        int columns = 7;

        board = new char[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                board[i][j] = '.';
            }
        }

        player1Turn = true;
        winner = -1;
    }

    // Returns a String containing instructions to play Connect Four.
    // Describes how to read the board, make moves, end condition, and how to win.
    public String instructions() {
        String result = "Connect Four: Player 1 uses 'X', Player 2 uses 'O', ";
        result += "empty spaces are '.'. The board shows columns 1-7 at the bottom. ";
        result += "To move, enter 'A <column>' to add a token (e.g., 'A 4') or ";
        result += "'R <column>' to remove your token from the bottom of a column ";
        result += "(e.g., 'R 3'). You can only remove your own tokens. ";
        result += "The game ends when a player gets four in a row (horizontally, ";
        result += "vertically, or diagonally) and that player wins, or the board ";
        result += "fills up resulting in a tie.";
        return result;
    }

    // Returns a String representation of the current Connect Four board state.
    // Shows all tokens on the board and column numbers at the bottom.
    public String toString() {
        int rows = board.length;
        int columns = board[0].length;

        for (int i = 0; i < rows; i++) {
            String row = "|";
            for (int j = 0; j < columns; j++) {
                row += " " + board[i][j] + " |";
            }
            System.out.println(row);
        }
       return "  1  2  3  4  5  6  7";
    }

    // Returns the index of the winner of the game.
    // 1 if Player 1 (X) won, 2 if Player 2 (O) won, 0 if a tie occurred,
    // and -1 if the game is not over.
    public int getWinner() {
        return winner;
    }

    // Returns the index of which player's turn it is.
    // 1 if Player 1 (X), 2 if Player 2 (O), -1 if the game is over.
    public int getNextPlayer() {
        if (winner != -1) {
            return -1;
        }
        if (player1Turn) {
            return 1;
        } else {
            return 2;
        }
    }

    // Reads the move from the given Scanner input and returns a string
    // representation of the action and column. Format: "<A|R> <column>"
    // Throws an IllegalArgumentException if input is null.
    public String getMove(Scanner input) {
        if (input == null) {
            throw new IllegalArgumentException();
        }
        System.out.print("Enter move (A/R column): ");
        String action = input.next();
        int column = input.nextInt();
        return action + " " + column;
    }

    // Given the input, either adds or removes a token based on the action.
    // Format: "<A|R> <column>" where A = add, R = remove.
    // Throws an IllegalArgumentException if input is null, if the column is
    // out of bounds, if the column is full (for add), or if the bottom token
    // is not the current player's (for remove).
    public void makeMove(String input) {
        if (input == null) {
            throw new IllegalArgumentException();
        }

        int spaceIndex = input.indexOf(" ");
        String action = input.substring(0, spaceIndex).toUpperCase();
        int col = Integer.parseInt(input.substring(spaceIndex + 1)) - 1;

        int columns = board[0].length;
        if (col < 0 || col >= columns) {
            throw new IllegalArgumentException();
        }

        char currentToken;
        if (player1Turn) {
            currentToken = 'X';
        } else {
            currentToken = 'O';
        }

        if (action.equals("A")) {
            addToken(col, currentToken);
        } else if (action.equals("R")) {
            removeToken(col, currentToken);
        } else {
            throw new IllegalArgumentException();
        }

        checkForWinner();
        player1Turn = !player1Turn;
    }

    // Adds a token to the specified column, dropping it to the lowest empty row.
    // Throws an IllegalArgumentException if the column is full.
    private void addToken(int col, char token) {
        if (board[0][col] != '.') {
            throw new IllegalArgumentException();
        }

        int targetRow = findLowestEmptyRow(col);
        board[targetRow][col] = token;
    }

    // Removes the current player's token from the bottom of the specified column
    // and shifts all tokens above it down by one row.
    // Throws an IllegalArgumentException if the column is empty or if the bottom
    // token does not belong to the current player.
    private void removeToken(int col, char currentToken) {
        int bottomRow = findBottomToken(col);

        if (bottomRow == -1) {
            throw new IllegalArgumentException();
        }

        if (board[bottomRow][col] != currentToken) {
            throw new IllegalArgumentException();
        }

        for (int row = bottomRow; row > 0; row--) {
            board[row][col] = board[row - 1][col];
        }
        board[0][col] = '.';
    }

    // Finds the row index of the bottom-most token in the specified column.
    // Returns -1 if the column is empty.
    private int findBottomToken(int col) {
        int rows = board.length;
        for (int row = rows - 1; row >= 0; row--) {
            if (board[row][col] != '.') {
                return row;
            }
        }
        return -1;
    }

    // Finds the lowest empty row in the specified column.
    // Returns the row index of the lowest empty space, or -1 if full.
    private int findLowestEmptyRow(int col) {
        int rows = board.length;
        for (int row = rows - 1; row >= 0; row--) {
            if (board[row][col] == '.') {
                return row;
            }
        }
        return -1;
    }

    // Checks the entire board for a winner or tie and updates the winner field.
    private void checkForWinner() {
        if (checkBoardForWin('X')) {
            winner = 1;
            return;
        }

        if (checkBoardForWin('O')) {
            winner = 2;
            return;
        }

        if (isBoardFull()) {
            winner = 0;
        }
    }

    // Checks if the specified token has four in a row anywhere on the board.
    private boolean checkBoardForWin(char token) {
        int rows = board.length;
        int columns = board[0].length;

        for (int row = 0; row < rows; row++) {
            if (checkHorizontal(row, token)) {
                return true;
            }
        }

        for (int col = 0; col < columns; col++) {
            if (checkVertical(col, token)) {
                return true;
            }
        }

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (checkDiagonalDown(row, col, token)) {
                    return true;
                }
                if (checkDiagonalUp(row, col, token)) {
                    return true;
                }
            }
        }

        return false;
    }

    // Checks for a horizontal win (four in a row) in the specified row.
    private boolean checkHorizontal(int row, char token) {
        int columns = board[0].length;
        int count = 0;
        for (int col = 0; col < columns; col++) {
            if (board[row][col] == token) {
                count++;
                if (count >= 4) {
                    return true;
                }
            } else {
                count = 0;
            }
        }
        return false;
    }

    // Checks for a vertical win (four in a row) in the specified column.
    private boolean checkVertical(int col, char token) {
        int rows = board.length;
        int count = 0;
        for (int row = 0; row < rows; row++) {
            if (board[row][col] == token) {
                count++;
                if (count >= 4) {
                    return true;
                }
            } else {
                count = 0;
            }
        }
        return false;
    }

    // Checks for a diagonal win going from top-left to bottom-right
    // starting from the specified position.
    private boolean checkDiagonalDown(int startRow, int startCol, char token) {
        int rows = board.length;
        int columns = board[0].length;
        int count = 0;
        int row = startRow;
        int col = startCol;
        while (row < rows && col < columns) {
            if (board[row][col] == token) {
                count++;
                if (count >= 4) {
                    return true;
                }
            } else {
                count = 0;
            }
            row++;
            col++;
        }
        return false;
    }

    // Checks for a diagonal win going from bottom-left to top-right
    // starting from the specified position.
    private boolean checkDiagonalUp(int startRow, int startCol, char token) {
        int columns = board[0].length;
        int count = 0;
        int row = startRow;
        int col = startCol;
        while (row >= 0 && col < columns) {
            if (board[row][col] == token) {
                count++;
                if (count >= 4) {
                    return true;
                }
            } else {
                count = 0;
            }
            row--;
            col++;
        }
        return false;
    }

    // Checks if the board is completely full (no empty spaces).
    // Returns true if full, false otherwise.
    private boolean isBoardFull() {
        int columns = board[0].length;
        for (int col = 0; col < columns; col++) {
            if (board[0][col] == '.') {
                return false;
            }
        }
        return true;
    }
}
