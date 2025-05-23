package hexlet.code.app.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskDTO {

    private Long id;

    private Integer index;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate createdAt;

    // assignee.id in the Task entity
    private Long assigneeId;

    // name in Task entity
    @Size(min = 1)
    private String title;

    // description in Task entity
    private String content;

    // taskStatus.slug in the Task entity
    private String status;
}
