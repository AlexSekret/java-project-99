package hexlet.code.app.exception;

public class StatusHasAssociatedTasksException extends RuntimeException {
    public StatusHasAssociatedTasksException(String message) {
        super(message);
    }
}
