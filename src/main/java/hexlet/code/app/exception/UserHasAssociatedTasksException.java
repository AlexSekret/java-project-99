package hexlet.code.app.exception;

public class UserHasAssociatedTasksException extends RuntimeException {
    public UserHasAssociatedTasksException(String message) {
        super(message);
    }
}
