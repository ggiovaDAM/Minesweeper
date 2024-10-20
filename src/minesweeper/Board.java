package minesweeper;

import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Board {

    private int width;
    private int height;
    private int[][] numberTable;
    private boolean[][] bombTable;
    private TileState[][] activeTable;
    private final int bombs;
    private final int boardArea;
    private int correctlyFlaggedBombs;
    private int clearedTiles;
    private int flags;
    private static final Scanner scanner = new Scanner(System.in);
    public BoardState boardState;


    public Board(final int height, final int width, final int bombs) throws IllegalArgumentException {
        if (width < 1) throw new IllegalArgumentException("The width must be at least 1.");
        if (width >= 100) throw new IllegalArgumentException("The width must be at max 100.");
        if (height < 1) throw new IllegalArgumentException("The height must be at least 1.");
        if (height >= 100) throw new IllegalArgumentException("The height must be at max 100.");
        this.width = width;
        this.height = height;
        if (bombs < 1) throw new IllegalArgumentException("The amount of bombs must be at least 1.");
        this.boardArea = width * height;
        if (bombs > this.boardArea) throw new IllegalArgumentException("The amount of bombs must be at max width × height (" + this.boardArea + ").");
        this.bombs = bombs;

        this.numberTable = new int[height][width];
        this.bombTable = new boolean[height][width];
        this.activeTable = new TileState[height][width];

        this.generateTable();

        this.clearedTiles = 0;
        this.flags = 0;

        this.boardState = BoardState.PLAYING;
    }

    public static Board boardEasy() {
        return new Board(10, 10, 10);
    }

    public static Board boardMedium() {
        return new Board(15, 15, 20);
    }

    public static Board boardHard() {
        return new Board(25, 25, 40);
    }

    private void generateTable() {
        for (int ii = 0; ii < this.height; ii++) {
            for (int jj = 0; jj < this.width; jj++) {
                this.numberTable[ii][jj] = 0;
                this.bombTable[ii][jj] = false;
                this.activeTable[ii][jj] = TileState.TILE;
            }
        }

        int bombsToPlace = this.bombs;
        int x, y;
        do {
            x = randomBetween(0, this.width - 1);
            y = randomBetween(0, this.height - 1);

            if (!this.bombTable[y][x]) {
                this.bombTable[y][x] = true;
                bombsToPlace--;
            }

        } while (bombsToPlace > 0);

        boolean isBomb;
        for (int ii = 0; ii < this.height; ii++) {
            for (int jj = 0; jj < this.width; jj++) {
                isBomb = this.bombTable[ii][jj];
                if (isBomb) {
                    // NIGHTMARE
                    if (ii - 1 >= 0) {
                        if (jj - 1 >= 0) this.numberTable[ii - 1][jj - 1] += 1;
                        this.numberTable[ii - 1][jj] += 1;
                        if (jj + 1 <= this.width - 1) this.numberTable[ii - 1][jj + 1] += 1;
                    }

                    if (jj - 1 >= 0) this.numberTable[ii][jj - 1] += 1;
                    if (jj + 1 <= this.width - 1) this.numberTable[ii][jj + 1] += 1;

                    if (ii + 1 <= this.height - 1) {
                        if (jj - 1 >= 0) this.numberTable[ii + 1][jj - 1] += 1;
                        this.numberTable[ii + 1][jj] += 1;
                        if (jj + 1 <= this.width - 1) this.numberTable[ii + 1][jj + 1] += 1;
                    }
                }
            }
        }
    }

    private void print() {
        StringBuilder stringBuilder = new StringBuilder();

        // Determines the total space required at the left of the table (for the index)
        final int spacingLeft = integerLength(this.height);
        // Determines the total space required in between each cell.
        final int spacingBetween = integerLength(this.width);

        stringBuilder.append(" ".repeat(spacingLeft)).append(" | ");

        for (int ii = 1; ii <= this.width; ii++) {
            stringBuilder.append(ii).append(" ".repeat(spacingBetween - integerLength(ii))).append(" ");
        }

        stringBuilder.append('\n').append("-".repeat(spacingLeft)).append("-+");
        for (int ii = 1; ii <= this.width; ii++) {
            stringBuilder.append("-".repeat(spacingBetween)).append("-");
        }
        stringBuilder.append('\n');

        for (int ii = 0; ii < this.height; ii++) {
            int heightIndex = ii + 1;
            stringBuilder.append(heightIndex).append(" ".repeat(spacingLeft - integerLength(heightIndex))).append(" | ");
            for (int jj = 0; jj < this.width; jj++) {
                if (boardState == BoardState.LOST && this.bombTable[ii][jj]) {
                    stringBuilder.append('X');
                } else {
                    TileState current = this.activeTable[ii][jj];

                    switch (current) {
                        case TILE -> stringBuilder.append('T');
                        case CLEAR -> {
                            int number = this.numberTable[ii][jj];

                            if (number == 0) stringBuilder.append(' ');
                            else stringBuilder.append(number);
                        }
                        case BOMB_FLAG -> stringBuilder.append('B');
                        case MAYBE_FLAG -> stringBuilder.append('M');
                    }
                }

                if (jj < this.width - 1) {
                    stringBuilder.append(" ".repeat(spacingBetween - 1)).append(' ');
                }
            }
            if (ii < this.height - 1) {
                stringBuilder.append('\n');
            }

        }

        System.out.println(stringBuilder);
    }

    public void step() throws ExitException {
        System.out.printf("\n\nThe tiles are named: T: unclear tile, B: mine flag, M: maybe mine flag, and empty for cleared tile." +
                          "\nThis game is %d tall and %d wide, with %d bombs!" +
                          "\n\nCleared tiles: %d, flagged mines: %d%n",
                this.height,
                this.width,
                this.bombs,
                this.clearedTiles,
                this.flags);
        print();
        requestIndex();
        checkGame();

    }

    private void checkGame() {
        if (this.correctlyFlaggedBombs == this.bombs || this.boardArea - this.bombs == this.clearedTiles) {
            this.boardState = BoardState.WON;
        }

        if (this.boardState == BoardState.WON) {
            won();
        } else if (this.boardState == BoardState.LOST) {
            lost();
        }
    }

    private void won() {
        print();
        System.out.println("""
                
                YOU WON THE GAME!
                """);
    }

    private void lost() {
        print();
        System.out.println("""
                
                YOU LOST THE GAME!
                """);
    }

    private void requestIndex() throws ExitException {
        String input, currentToken;
        boolean fails;
        StringTokenizer tokens;
        TileState newTileState = null;
        int x, y;
        do {
            fails = false;
            System.out.println("Write: '[clear, flag, maybe] y x'");
            input = scanner.nextLine().toLowerCase();
            tokens = new StringTokenizer(input);

            currentToken = tokens.nextToken();
            switch (currentToken) {
                case "end" -> throw new ExitException("Exit request");
                case "clear" -> newTileState = TileState.CLEAR;
                case "flag" -> newTileState = TileState.BOMB_FLAG;
                case "maybe" -> newTileState = TileState.MAYBE_FLAG;
                default -> {
                    fails = true;
                    System.out.println("Command not recognized");
                }
            }

            if (!fails) {
                currentToken = tokens.nextToken();
                if (currentToken.matches("\\+?[0-9]{1,6}")) {
                    y = Integer.parseInt(currentToken);

                    currentToken = tokens.nextToken();
                    if (currentToken.matches("\\+?[0-9]{1,6}")) {
                        x = Integer.parseInt(currentToken);

                        String result = interactBoard(newTileState, y, x);
                        if (result != null) {
                            fails = true;
                            System.out.println(result);
                        }

                    } else {
                        System.out.println("Unknown token (" + currentToken + ").");
                    }
                } else {
                    System.out.println("Unknown token (" + currentToken + ").");
                }

            }

        } while (fails);
    }

    private String interactBoard(final TileState tileState, final int y, final int x) {
        int sy = y - 1, sx = x - 1;
        try {
            boolean isBomb = this.bombTable[sy][sx];
            switch (tileState) {
                case CLEAR -> {
                    switch(this.activeTable[sy][sx]) {
                        case CLEAR -> {
                            return "Tile already cleared.";
                        }
                        case BOMB_FLAG -> this.flags--;
                    }

                    if (isBomb) {
                        this.boardState = BoardState.LOST;
                    } else {
                        clearCascade(sy, sx);
                    }
                    return null;
                }
                case BOMB_FLAG -> {
                    switch(this.activeTable[sy][sx]) {
                        case TILE, MAYBE_FLAG -> {
                            this.activeTable[sy][sx] = TileState.BOMB_FLAG;
                            this.correctlyFlaggedBombs++;
                            this.flags++;
                        }
                        case CLEAR -> {
                            return "Tile already cleared.";
                        }
                        case BOMB_FLAG -> {
                            this.activeTable[sy][sx] = TileState.TILE;
                            this.correctlyFlaggedBombs--;
                            this.flags--;
                        }
                    }
                }
                case MAYBE_FLAG -> {
                    switch(this.activeTable[sy][sx]) {
                        case TILE -> this.activeTable[sy][sx] = TileState.MAYBE_FLAG;
                            case BOMB_FLAG -> {
                                this.activeTable[sy][sx] = TileState.MAYBE_FLAG;
                                this.correctlyFlaggedBombs--;
                                this.flags--;
                            }
                        case CLEAR -> {
                            return "Tile already cleared.";
                        }
                        case MAYBE_FLAG -> this.activeTable[sy][sx] = TileState.TILE;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException exception) {
            return "Either y (%d) or x (%d) or both fall out of bounds".formatted(y, x);
        }
        return null;
    }

    /*Looping nightmare*/
    private void clearCascade(int y, int x) throws ArrayIndexOutOfBoundsException {
        if (this.numberTable[y][x] == 0 && this.activeTable[y][x] == TileState.TILE) {
            this.activeTable[y][x] = TileState.CLEAR;
            if (y - 1 >= 0 && x - 1 >= 0)                  if (this.activeTable[y - 1][x - 1] == TileState.TILE) clearCascade(y - 1, x - 1);
            if (y - 1 >= 0)                                if (this.activeTable[y - 1][x    ] == TileState.TILE) clearCascade(y - 1, x);
            if (y - 1 >= 0 && x + 1 < this.width)          if (this.activeTable[y - 1][x + 1] == TileState.TILE) clearCascade(y - 1, x + 1);
            if (x - 1 >= 0)                                if (this.activeTable[y    ][x - 1] == TileState.TILE) clearCascade(y, x - 1);
            if (x + 1 < this.width)                        if (this.activeTable[y    ][x + 1] == TileState.TILE) clearCascade(y, x + 1);
            if (y + 1 < this.height && x - 1 >= 0)         if (this.activeTable[y + 1][x - 1] == TileState.TILE) clearCascade(y + 1, x - 1);
            if (y + 1 < this.height)                       if (this.activeTable[y + 1][x    ] == TileState.TILE) clearCascade(y + 1, x);
            if (y + 1 < this.height && x + 1 < this.width) if (this.activeTable[y + 1][x + 1] == TileState.TILE) clearCascade(y + 1, x + 1);
        } else {
            this.activeTable[y][x] = TileState.CLEAR;
            // Uncomment to see the iterations
            //print();
        }
        this.clearedTiles++;
    }

    private static int integerLength(final int number) {
        return Integer.toString(number).length();
    }

    private static int randomBetween(final int min, final int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

}
