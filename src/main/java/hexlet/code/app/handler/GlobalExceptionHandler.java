package hexlet.code.app.handler;

import hexlet.code.app.exception.DuplicateEntitySaveException;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.exception.StatusHasAssociatedTasksException;
import hexlet.code.app.exception.UserHasAssociatedTasksException;
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

    @ExceptionHandler(UserHasAssociatedTasksException.class)
    public ResponseEntity<String> handleUserHasAssociatedTasksException(UserHasAssociatedTasksException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(StatusHasAssociatedTasksException.class)
    public ResponseEntity<String> handleStatusHasAssociatedTasksException(StatusHasAssociatedTasksException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
