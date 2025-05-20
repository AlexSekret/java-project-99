package hexlet.code.app.service;

import hexlet.code.app.dto.TaskCreateDTO;
import hexlet.code.app.dto.TaskDTO;
import hexlet.code.app.dto.TaskUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.model.Label;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    @Autowired
    private LabelRepository labelRepository;

    public List<TaskDTO> getAll() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(taskMapper::toTaskDTO)
                .toList();
    }

    public Page<TaskDTO> getPage(Pageable page) {
        Page<Task> tasks = taskRepository.findAll(page);
        Page<TaskDTO> result = tasks.map(taskMapper::toTaskDTO);
        return result;
    }

    public TaskDTO getById(Long id) {
        Task task = taskRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        return taskMapper.toTaskDTO(task);
    }

    public TaskDTO create(TaskCreateDTO dto) {
        List<Long> labelIds = dto.getTaskLabelIds();
        String slug = dto.getStatus();
        Task task = taskMapper.toTaskModel(dto);
        Long assigneeId = dto.getAssigneeId();
        TaskStatus status = taskStatusRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("TaskStatus with slug " + slug + " not found"));
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + assigneeId + " not found"));
        List<Label> labels = labelIds.stream()
                .map(id -> labelRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Label with id " + id + " not found")))
                .toList();
        task.setTaskStatus(status);
        task.setAssignee(assignee);
        task.setLabels(labels);
        labels.forEach(label -> {
            label.getTasks().add(task);
        });
        taskRepository.save(task);
        TaskDTO result = taskMapper.toTaskDTO(task);
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
