package hexlet.code.app.controller;

import hexlet.code.app.dto.TaskStatusCreateDTO;
import hexlet.code.app.dto.TaskStatusDTO;
import hexlet.code.app.dto.TaskStatusUpdateDTO;
import hexlet.code.app.service.TaskStatusService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
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
public class TaskStatusController {
    @Autowired
    private TaskStatusService taskStatusService;

    @GetMapping(path = "/task_statuses/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskStatusDTO show(@PathVariable Long id) {
        return taskStatusService.getById(id);
    }

    @GetMapping(path = "/task_statuses")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<TaskStatusDTO>> index(
            @RequestParam(name = "_end", defaultValue = "5") int end,
            @RequestParam(name = "_start", defaultValue = "0") int start,
            @RequestParam(name = "_sort", defaultValue = "id") String sort,
            @RequestParam(name = "_order", defaultValue = "ASC") String order) {
//        https://task-manager-7ylq.onrender.com/api/task_statuses?_end=10&_order=ASC&_sort=id&_start=0
        int perPage = end - start;
        int pageNumber = (int) Math.ceil((double) start / perPage);
        Sort.Direction sotrDirection = Sort.Direction.valueOf(order);

        Pageable pageRequest = PageRequest.of(pageNumber, perPage, sotrDirection, sort);
        Page<TaskStatusDTO> tasksPage = taskStatusService.getPage(pageRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(tasksPage.getTotalElements()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(tasksPage.getContent());
    }

    @PostMapping(path = "/task_statuses")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskStatusDTO create(@Valid @RequestBody TaskStatusCreateDTO dto) {
        return taskStatusService.create(dto);
    }

    @PutMapping(path = "/task_statuses/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskStatusDTO update(@PathVariable Long id, @Valid @RequestBody TaskStatusUpdateDTO dto) {
        return taskStatusService.update(id, dto);
    }

    @DeleteMapping(path = "/task_statuses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskStatusService.deleteById(id);
    }

}
