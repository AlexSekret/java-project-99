package hexlet.code.handler;

import hexlet.code.exception.DuplicateEntitySaveException;
import hexlet.code.exception.EntityHasAssociatedTaskException;
import hexlet.code.exception.ResourceNotFoundException;
import io.sentry.Sentry;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateEntitySaveException.class)
    public ResponseEntity<String> handleDuplicateSlugException(DuplicateEntitySaveException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(EntityHasAssociatedTaskException.class)
    public ResponseEntity<String> handleEntityHasAssociatedTaskException(EntityHasAssociatedTaskException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation() {
        return ResponseEntity.badRequest()
                .body("Cannot delete entity because it has associated records");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        Sentry.captureException(ex); // Отправка в Sentry
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal Server Error: " + ex.getMessage());
    }
}
