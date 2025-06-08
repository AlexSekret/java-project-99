package hexlet.code.service;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.exception.DuplicateEntitySaveException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LabelService {
    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;

    public LabelDTO getById(Long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "Label"));
        return labelMapper.toDto(label);
    }

    public Page<LabelDTO> getPage(Pageable page) {
        Page<Label> labels = labelRepository.findAll(page);
        return labels.map(labelMapper::toDto);
    }

    public LabelDTO create(@Valid LabelCreateDTO dto) {
        if (labelRepository.findByName(dto.getName()).isPresent()) {
            throw new DuplicateEntitySaveException("Label with name " + dto.getName() + " already exists");
        }
        Label label = labelMapper.toModel(dto);
        labelRepository.save(label);
        return labelMapper.toDto(label);
    }

    public void delete(Long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "Label"));
        labelRepository.delete(label);
    }

    public LabelDTO update(Long id, LabelUpdateDTO dto) {
        Label model = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "Label"));
        labelMapper.update(dto, model);
        labelRepository.save(model);
        return labelMapper.toDto(model);
    }
}
