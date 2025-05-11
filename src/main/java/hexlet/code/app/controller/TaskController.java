package hexlet.code.app.controller;

import hexlet.code.app.dto.TaskCreateDTO;
import hexlet.code.app.dto.TaskDTO;
import hexlet.code.app.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
        int perPage = end - start;
        int pageNumber = (int) Math.ceil((double) start / perPage);
        Sort.Direction sotrDirection = Sort.Direction.valueOf(order);
        Pageable pageRequest = PageRequest.of(pageNumber, perPage, sotrDirection, sort);
        Page<TaskDTO> page = taskService.getPage(pageRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
        return ResponseEntity.ok()
                .headers(headers)
                .body(page.getContent());
    }
    @PostMapping(path = "/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO create(@Valid @RequestBody TaskCreateDTO dto) {
        return taskService.create(dto);
    }
}
