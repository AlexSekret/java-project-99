package hexlet.code.app.mapper;

import hexlet.code.app.dto.LabelDTO;
import hexlet.code.app.model.Label;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class LabelMapper {
    public abstract LabelDTO toDto(Label model);

//    @Mapping(target = "tasks", ignore = true)
//    public abstract Label toEntity(LabelDTO labelDTO);
}
