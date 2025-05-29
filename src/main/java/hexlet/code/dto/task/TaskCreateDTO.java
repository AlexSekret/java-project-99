package hexlet.code.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class TaskCreateDTO {
    private Integer index;

    @JsonProperty(value = "assignee_id")
    private Long assigneeId;

    @NotBlank(message = "Title cannot be empty")
    @Size(min = 1)
    private String title;

    private String content;
    @NotBlank(message = "Status cannot be empty")
    private String status;
    private Set<Long> taskLabelIds = new HashSet<>();
}
