package util.exception;

public class CancelledException extends RuntimeException {
    public CancelledException() {
        super("Operation cancelled by the user");
    }
}
