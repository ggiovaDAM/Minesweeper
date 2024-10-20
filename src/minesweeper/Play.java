package minesweeper;

import java.util.Scanner;
import java.util.StringTokenizer;

public class Play {

    private static final Scanner scanner = new Scanner(System.in);
    private GameState gameState;
    private Board board;


    public Play() {
        this.gameState = GameState.BOOTUP;
        do {
            switch (this.gameState) {
                case BOOTUP -> bootup();
                case INTERMISSION -> intermission();
                case PLAY -> play();
            }
        } while(this.gameState != GameState.END);
        end();
    }

    private void bootup() {
        System.out.println("""
        Welcome to Minesweeper!
        The rules of the game are simple:
         * Select the tiles on the board to uncover them
         * If you choose a tile where a mine is, you lose
         * Flag all the tiles that have a mine underneath
         * You can flag tiles that you believe might have a mine
         * The game is won if all the tiles with mines are flagged as having mines, no more, no less
         
        Have fun!
        (write 'start' or 'quit')""");

        int action = requestStart();

        if (action == 1) {
            this.gameState = GameState.INTERMISSION;
        } else {
            this.gameState = GameState.END;
        }
    }

    private void intermission() {
        System.out.println("""
        To generate the board, write: 'play height width bombs'
                            Or write: 'play [easy, medium, hard]'
            For example: 'play 12 15 20' or 'play medium'""");
        this.board = null;
        try {
            requestBoard();
            this.gameState = GameState.PLAY;
        } catch (ExitException ee) {
            this.gameState = GameState.END;
        }
    }

    private void requestBoard() throws ExitException {
        String input, currentToken;
        StringTokenizer tokens;
        do {
            input = scanner.nextLine().toLowerCase();
            tokens = new StringTokenizer(input);

            currentToken = tokens.nextToken();
            if (currentToken.equals("end")) {
                throw new ExitException("Exit request");
            } else if (currentToken.equals("play")) {
                currentToken = tokens.nextToken();
                if (currentToken.equals("easy")) {
                    this.board = Board.boardEasy();
                } else if (currentToken.equals("medium")) {
                    this.board = Board.boardMedium();
                } else if (currentToken.equals("hard")) {
                    this.board = Board.boardHard();
                } else if (currentToken.matches("\\+?[0-9]{1,6}")) {
                    int height = Integer.parseInt(currentToken);
                    currentToken = tokens.nextToken();
                    if (currentToken.matches("\\+?[0-9]{1,6}")) {
                        int width = Integer.parseInt(currentToken);
                        currentToken = tokens.nextToken();
                        if (currentToken.matches("\\+?[0-9]{1,6}")) {
                            int bombs = Integer.parseInt(currentToken);

                            try {
                                this.board = new Board(height, width, bombs);
                            } catch (IllegalArgumentException iae) {
                                System.out.println(iae.getMessage());
                            }
                        } else {
                            System.out.println(encapsulate("Unknown token ", currentToken));
                        }
                    } else {
                        System.out.println(encapsulate("Unknown token ", currentToken));
                    }
                } else {
                    System.out.println(encapsulate("Unknown token ", currentToken));
                }
            } else {
                System.out.println("Command not recognized");
            }
        } while(this.board == null);
    }

    private void play() {
        try {
            this.board.step();
            if (this.board.boardState != BoardState.PLAYING) {
                this.gameState = GameState.INTERMISSION;
            }
        } catch (ExitException ee) {
            this.gameState = GameState.INTERMISSION;
        }
    }

    private static int requestStart() {
        String input;
        do {
            input = scanner.nextLine().toLowerCase();
            if (input.equals("start")) {
                return 1;
            } else if (input.equals("quit")) {
                return -1;
            } else {
                System.out.println(encapsulate("Value not recognized ", input) + ".\nPlease introduce either 'start' or 'quit'");
            }
        } while (true);
    }

    private void end() {
        System.out.println("""
                
                Good bye!
                """);
    }

    private static String encapsulate(String pre, String encased) {
        StringBuilder stringBuilder = new StringBuilder(pre);
        if (encased.length() < 20) {
            stringBuilder.append('(').append(encased).append(')');
        }
        return stringBuilder.toString();
    }

}