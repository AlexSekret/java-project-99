package hexlet.code.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(Long id, String entityName) {
        super(String.format("%s with id %d not found", entityName, id));
    }
}
