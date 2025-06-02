package hexlet.code.service;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.exception.DuplicateEntitySaveException;
import hexlet.code.exception.EntityHasAssociatedTaskException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LabelService {
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private LabelMapper labelMapper;

    public LabelDTO getById(Long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id " + id + " not found"));
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
        //Если метка связана с задачей, удалить её нельзя
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id " + id + " not found"));
        try {
            labelRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new EntityHasAssociatedTaskException("Label with id: {" + id + "} has a associated task"
                    + " and can not be deleted");
        }
    }

    public LabelDTO update(Long id, LabelUpdateDTO dto) {
        Label model = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label with id " + id + " not found"));
        labelMapper.update(dto, model);
        labelRepository.save(model);
        return labelMapper.toDto(model);
    }
}
