package hexlet.code.service;

import hexlet.code.dto.status.TaskStatusCreateDTO;
import hexlet.code.dto.status.TaskStatusDTO;
import hexlet.code.dto.status.TaskStatusUpdateDTO;
import hexlet.code.exception.DuplicateEntitySaveException;
import hexlet.code.exception.EntityHasAssociatedTaskException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
        try {
            tsRepository.delete(taskStatus);
        } catch (DataIntegrityViolationException e) {
            throw new EntityHasAssociatedTaskException("Task Status with id " + id + " has tasks and can't be deleted");
        }
    }

    public TaskStatusDTO create(TaskStatusCreateDTO dto) {
        TaskStatus entity = tsMapper.mapToModel(dto);
        Optional<TaskStatus> slug = tsRepository.findBySlug(entity.getSlug());
        if (slug.isEmpty()) {
            TaskStatus model = tsMapper.mapToModel(dto);
            tsRepository.save(model);
            return tsMapper.mapToDto(model);
        } else {
            throw new DuplicateEntitySaveException("Task status with slug " + dto.getSlug() + " already exists");
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
            throw new DuplicateEntitySaveException("Task status with slug " + dto.getSlug() + " already exists");
        }
    }

    public Page<TaskStatusDTO> getPage(Pageable page) {
        Page<TaskStatus> tasks = tsRepository.findAll(page);
        return tasks.map(tsMapper::mapToDto);
    }
}
