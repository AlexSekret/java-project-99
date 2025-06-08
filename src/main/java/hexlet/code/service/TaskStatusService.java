package hexlet.code.service;

import hexlet.code.dto.status.TaskStatusCreateDTO;
import hexlet.code.dto.status.TaskStatusDTO;
import hexlet.code.dto.status.TaskStatusUpdateDTO;
import hexlet.code.exception.DuplicateEntitySaveException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class TaskStatusService {
    private final TaskStatusRepository tsRepository;
    private final TaskStatusMapper tsMapper;
    private static final String STATUS_WITH_SLUG_ALREADY_EXIST = "Task status with slug %s already exists";

    public TaskStatusDTO getById(Long id) {
        TaskStatus taskStatus = tsRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(id, "Task status"));
        return tsMapper.mapToDto(taskStatus);
    }

    public void deleteById(Long id) {
        TaskStatus taskStatus = tsRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(id, "Task status"));
        tsRepository.delete(taskStatus);
    }

    public TaskStatusDTO create(TaskStatusCreateDTO dto) {
        TaskStatus entity = tsMapper.mapToModel(dto);
        Optional<TaskStatus> slug = tsRepository.findBySlug(entity.getSlug());
        if (slug.isEmpty()) {
            TaskStatus model = tsMapper.mapToModel(dto);
            tsRepository.save(model);
            return tsMapper.mapToDto(model);
        } else {
            throw new DuplicateEntitySaveException(String.format(STATUS_WITH_SLUG_ALREADY_EXIST, dto.getSlug()));
        }
    }

    public TaskStatusDTO update(Long id, TaskStatusUpdateDTO dto) {
        TaskStatus model = tsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with id " + id + " not found"));
        TaskStatus entity = tsMapper.mapToModel(dto);
        Optional<TaskStatus> slug = tsRepository.findBySlug(entity.getSlug());
        if (slug.isEmpty() || !(slug.get().getSlug().equals(model.getSlug()))) {
            tsMapper.update(dto, model);
            tsRepository.save(model);
            return tsMapper.mapToDto(model);
        } else {
            throw new DuplicateEntitySaveException(String.format(STATUS_WITH_SLUG_ALREADY_EXIST, dto.getSlug()));
        }
    }

    public Page<TaskStatusDTO> getPage(Pageable page) {
        Page<TaskStatus> tasks = tsRepository.findAll(page);
        return tasks.map(tsMapper::mapToDto);
    }
}
