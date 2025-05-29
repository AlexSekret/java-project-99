package hexlet.code.exception;

public class DuplicateEntitySaveException extends RuntimeException {
    public DuplicateEntitySaveException(String message) {
        super(message);
    }
}
