package hexlet.code.app.service;

import hexlet.code.app.dto.LabelCreateDTO;
import hexlet.code.app.dto.LabelDTO;
import hexlet.code.app.exception.DuplicateEntitySaveException;
import hexlet.code.app.exception.EntityHasAssociatedTaskException;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.model.Label;
import hexlet.code.app.repository.LabelRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
        if (label.getTasks().isEmpty()) {
            labelRepository.deleteById(id);
        } else {
            throw new EntityHasAssociatedTaskException("Label with id: {" + id + "} has a associated task" +
                    " and can not be deleted");
        }
    }
}
