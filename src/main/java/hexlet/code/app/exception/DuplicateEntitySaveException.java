package hexlet.code.app.exception;

public class DuplicateEntitySaveException extends RuntimeException {
    public DuplicateEntitySaveException(String message) {
        super(message);
    }
}
