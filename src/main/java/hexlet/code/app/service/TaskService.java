package hexlet.code.app.service;

import hexlet.code.app.dto.TaskCreateDTO;
import hexlet.code.app.dto.TaskDTO;
import hexlet.code.app.dto.TaskUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private UserRepository userRepository;


    public List<TaskDTO> getAll() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(taskMapper::toTaskDTO)
                .toList();
    }

    public Page<TaskDTO> getPage(Pageable page) {
        Page<Task> tasks = taskRepository.findAll(page);
        return tasks.map(taskMapper::toTaskDTO);
    }

    public TaskDTO getById(Long id) {
        Task task = taskRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        return taskMapper.toTaskDTO(task);
    }

    public TaskDTO create(TaskCreateDTO dto) {
        String slug = dto.getStatus();
        Task entity = taskMapper.toTaskModel(dto);
        Long assigneeId = dto.getAssigneeId();
        TaskStatus status = taskStatusRepository.findBySlug(dto.getStatus())
                .orElseThrow(() -> new ResourceNotFoundException("TaskStatus with slug " + slug + " not found"));
        User assignee = userRepository.findById(assigneeId).
                orElseThrow(() -> new ResourceNotFoundException("User with id " + assigneeId + " not found"));
        entity.setTaskStatus(status);
        entity.setAssignee(assignee);
        taskRepository.save(entity);
        TaskDTO result = taskMapper.toTaskDTO(entity);
        return result;
    }

    public void deleteById(Long id) {
        Task task = taskRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        taskRepository.deleteById(id);
    }

    public TaskDTO update(Long id, TaskUpdateDTO dto) {
        Task task = taskRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        taskMapper.update(dto, task);
        taskRepository.save(task);
        TaskDTO result = taskMapper.toTaskDTO(task);
        return result;
    }
}
