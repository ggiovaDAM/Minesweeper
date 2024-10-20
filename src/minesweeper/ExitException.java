package minesweeper;

public class ExitException extends Exception {

    public ExitException(String v) {
        super(v);
    }

    public ExitException() {
        super();
    }
}
