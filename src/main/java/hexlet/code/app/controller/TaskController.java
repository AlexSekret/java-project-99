package hexlet.code.app.controller;

import hexlet.code.app.dto.TaskCreateDTO;
import hexlet.code.app.dto.TaskDTO;
import hexlet.code.app.dto.TaskUpdateDTO;
import hexlet.code.app.service.PaginationService;
import hexlet.code.app.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TaskController {
    @Autowired
    private TaskService taskService;
    @Autowired
    private PaginationService paginationService;

    @GetMapping(path = "/tasks/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskDTO show(@PathVariable Long id) {
        return taskService.getById(id);
    }

    @GetMapping(path = "/tasks")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<TaskDTO>> index(
            @RequestParam(name = "_end", defaultValue = "10") int end,
            @RequestParam(name = "_start", defaultValue = "0") int start,
            @RequestParam(name = "_sort", defaultValue = "index") String sort,
            @RequestParam(name = "_order", defaultValue = "ASC") String order) {
        return paginationService.getPaginatedResponse(end, start, sort, order, taskService::getPage);
    }

    @PostMapping(path = "/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO create(@Valid @RequestBody TaskCreateDTO dto) {
        return taskService.create(dto);
    }

    @PutMapping(path = "/tasks/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskDTO update(@PathVariable Long id, @Valid @RequestBody TaskUpdateDTO dto) {
        return taskService.update(id, dto);
    }

    @DeleteMapping(path = "/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskService.deleteById(id);
    }
}
