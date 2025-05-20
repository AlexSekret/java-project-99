package hexlet.code.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TaskCreateDTO {
    private Integer index;
    private Long assigneeId;

    @NotBlank(message = "Title cannot be empty")
    @Size(min = 1)
    private String title;

    private String content;
    @NotBlank(message = "Status cannot be empty")
    private String status;
    private List<Long> taskLabelIds = new ArrayList<>();
}
