package hexlet.code.app.service;

import hexlet.code.app.dto.TaskStatusCreateDTO;
import hexlet.code.app.dto.TaskStatusDTO;
import hexlet.code.app.dto.TaskStatusUpdateDTO;
import hexlet.code.app.exception.DuplicateSlugException;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.exception.StatusHasAssociatedTasksException;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskStatusService {
    @Autowired
    private TaskStatusRepository tsRepository;

    @Autowired
    private TaskStatusMapper tsMapper;

    public List<TaskStatusDTO> getAll() {
        List<TaskStatus> taskStatuses = tsRepository.findAll();
        return taskStatuses.stream()
                .map(tsMapper::mapToDto)
                .toList();
    }

    public TaskStatusDTO getById(Long id) {
        TaskStatus taskStatus = tsRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Task status with id " + id + " not found"));
        return tsMapper.mapToDto(taskStatus);
    }

    public void deleteById(Long id) {
        TaskStatus taskStatus = tsRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Task status with id " + id + " not found"));
        if (taskStatus.getTasks().isEmpty()) {
            tsRepository.deleteById(id);
        } else {
            throw new StatusHasAssociatedTasksException("Task Status with id " + id + " has task and can't be deleted");
        }
    }

    public TaskStatusDTO create(TaskStatusCreateDTO dto) {
        TaskStatus entity = tsMapper.mapToModel(dto);
        Optional<TaskStatus> slug = tsRepository.findBySlug(entity.getSlug());
        if (slug.isEmpty()) {
            TaskStatus model = tsMapper.mapToModel(dto);
            tsRepository.save(model);
            TaskStatusDTO result = tsMapper.mapToDto(model);
            return result;
        } else {
            throw new DuplicateSlugException("Task status with slug " + dto.getSlug() + " already exists");
        }
    }

    public TaskStatusDTO update(Long id, TaskStatusUpdateDTO dto) {
        TaskStatus model = tsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with id " + id + " not found"));
        TaskStatus entity = tsMapper.mapToModel(dto);
        Optional<TaskStatus> slug = tsRepository.findBySlug(entity.getSlug());
        if (slug.isEmpty()) {
            tsMapper.update(dto, model);
            tsRepository.save(model);
            return tsMapper.mapToDto(model);
        } else {
            throw new DuplicateSlugException("Task status with slug " + dto.getSlug() + " already exists");
        }
    }

    public Page<TaskStatusDTO> getPage(Pageable page) {
        Page<TaskStatus> tasks = tsRepository.findAll(page);
        return tasks.map(tsMapper::mapToDto);
    }
}
