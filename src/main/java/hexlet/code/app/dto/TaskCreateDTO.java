package hexlet.code.app.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskCreateDTO {
    private Integer index;
    private Long assigneeId;

    @Size(min = 1)
    private String title;

    private String content;
    private String status;
}
