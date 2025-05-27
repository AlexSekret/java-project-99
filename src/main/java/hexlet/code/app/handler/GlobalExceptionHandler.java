package hexlet.code.app.handler;

import hexlet.code.app.exception.DuplicateEntitySaveException;
import hexlet.code.app.exception.EntityHasAssociatedTaskException;
import hexlet.code.app.exception.ResourceNotFoundException;
import io.sentry.Sentry;
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
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(EntityHasAssociatedTaskException.class)
    public ResponseEntity<String> handleEntityHasAssociatedTaskException(EntityHasAssociatedTaskException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // Новый обработчик для ВСЕХ неожиданных ошибок
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        Sentry.captureException(ex); // Отправка в Sentry
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal Server Error: " + ex.getMessage());
    }
}
