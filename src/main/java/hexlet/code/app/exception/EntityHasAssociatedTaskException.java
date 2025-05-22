package hexlet.code.app.exception;

public class EntityHasAssociatedTaskException extends RuntimeException {
    public EntityHasAssociatedTaskException(String message) {
        super(message);
    }
}
