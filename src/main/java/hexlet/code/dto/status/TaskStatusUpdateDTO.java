package hexlet.code.dto.status;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class TaskStatusUpdateDTO {

    @Size(min = 1)
    private JsonNullable<String> name;

    @Size(min = 1)
    @NotNull
    @NotBlank
    private JsonNullable<String> slug;
}
