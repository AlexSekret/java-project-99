package hexlet.code.service;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.specification.TaskSpecification;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final TaskSpecification specBuilder;

    public Page<TaskDTO> getPage(TaskParamsDTO params, Pageable page) {
        Specification<Task> spec = specBuilder.build(params);
        Page<Task> tasks = taskRepository.findAll(spec, page);
        return tasks.map(taskMapper::toTaskDTO);
    }

    public TaskDTO getById(Long id) {
        Task task = taskRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(id, "Task"));
        return taskMapper.toTaskDTO(task);
    }

    public TaskDTO create(TaskCreateDTO dto) {
        Task task = taskMapper.toTaskModel(dto);
        taskRepository.save(task);
        return taskMapper.toTaskDTO(task);
    }

    public void deleteById(Long id) {
        Task task = taskRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(id, "Task"));
        taskRepository.delete(task);
    }

    public TaskDTO update(Long id, TaskUpdateDTO dto) {
        Task task = taskRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(id, "Task"));
        taskMapper.update(dto, task);
        taskRepository.save(task);
        return taskMapper.toTaskDTO(task);
    }
}
